package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.*;
import com.example.ProyectoGym.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProgresoService {

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private AsignacionRutinaRepository asignacionRutinaRepository;

    @Autowired
    private SesionCompletadaRepository sesionCompletadaRepository;


    public List<Map<String, Object>> obtenerMiembrosConRutinas() {
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();

        return miembrosActivos.stream()
                .filter(miembro -> asignacionRutinaRepository.existsByMiembroAndActivoTrue(miembro))
                .map(miembro -> {
                    Map<String, Object> info = new HashMap<>();

                    Optional<AsignacionRutina> asignacionOpt = asignacionRutinaRepository
                            .findByMiembroAndActivoTrue(miembro);

                    if (asignacionOpt.isPresent()) {
                        AsignacionRutina asignacion = asignacionOpt.get();
                        RutinaPredefinida rutina = asignacion.getRutinaPredefinida();

                        info.put("miembroId", miembro.getId());
                        info.put("miembroNombre", miembro.getNombre());
                        info.put("miembroDni", miembro.getDni());
                        info.put("rutinaNombre", rutina.getNombre());
                        info.put("objetivo", asignacion.getObjetivoSeleccionado());
                        info.put("nivel", asignacion.getNivelSeleccionado());
                        info.put("frecuenciaSemanal", rutina.getFrecuenciaSemanal());

                        // Calcular sesiones del mes
                        Long sesionesEsteMes = sesionCompletadaRepository
                                .countSesionesEsteMes(miembro);

                        // Meta mensual
                        int metaMensual = rutina.getFrecuenciaSemanal() * 4;

                        // Calcular porcentaje
                        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);

                        info.put("sesionesCompletadas", sesionesEsteMes);
                        info.put("metaMensual", metaMensual);
                        info.put("porcentajeProgreso", Math.min(porcentaje, 100));
                        info.put("fechaAsignacion", asignacion.getFechaAsignacion());
                    }

                    return info;
                })
                .sorted((a, b) -> ((Integer) b.get("porcentajeProgreso"))
                        .compareTo((Integer) a.get("porcentajeProgreso")))
                .toList();
    }


    public Map<String, Object> obtenerDetalleProgreso(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return null;
        }

        Optional<AsignacionRutina> asignacionOpt = asignacionRutinaRepository
                .findByMiembroAndActivoTrue(miembro);

        if (!asignacionOpt.isPresent()) {
            return null;
        }

        AsignacionRutina asignacion = asignacionOpt.get();
        RutinaPredefinida rutina = asignacion.getRutinaPredefinida();

        Map<String, Object> detalle = new HashMap<>();

        // Información del miembro
        detalle.put("miembroId", miembro.getId());
        detalle.put("miembroNombre", miembro.getNombre());
        detalle.put("miembroDni", miembro.getDni());
        detalle.put("miembroEmail", miembro.getEmail());

        // Información de la rutina
        detalle.put("rutinaNombre", rutina.getNombre());
        detalle.put("rutinaDescripcion", rutina.getDescripcion());
        detalle.put("objetivo", asignacion.getObjetivoSeleccionado());
        detalle.put("nivel", asignacion.getNivelSeleccionado());
        detalle.put("duracion", rutina.getDuracion());
        detalle.put("frecuenciaSemanal", rutina.getFrecuenciaSemanal());
        detalle.put("fechaAsignacion", asignacion.getFechaAsignacion());

        // Estadísticas de progreso
        Long sesionesEsteMes = sesionCompletadaRepository.countSesionesEsteMes(miembro);
        Long totalSesiones = sesionCompletadaRepository.countByMiembro(miembro);
        int metaMensual = rutina.getFrecuenciaSemanal() * 4;
        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);

        detalle.put("sesionesEsteMes", sesionesEsteMes);
        detalle.put("totalSesiones", totalSesiones);
        detalle.put("metaMensual", metaMensual);
        detalle.put("porcentajeProgreso", Math.min(porcentaje, 100));
        detalle.put("sesionesFaltantes", Math.max(0, metaMensual - sesionesEsteMes));

        // Última sesión
        SesionCompletada ultimaSesion = sesionCompletadaRepository
                .findFirstByMiembroOrderByFechaCompletadaDesc(miembro);

        if (ultimaSesion != null) {
            detalle.put("ultimaSesion", ultimaSesion.getFechaCompletada());
        } else {
            detalle.put("ultimaSesion", null);
        }

        // Historial de sesiones
        List<SesionCompletada> historial = sesionCompletadaRepository
                .findByMiembroOrderByFechaCompletadaDesc(miembro);

        List<Map<String, Object>> historialSimplificado = historial.stream()
                .limit(10)
                .map(sesion -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("id", sesion.getId());
                    s.put("fecha", sesion.getFechaCompletada());
                    s.put("observaciones", sesion.getObservaciones());
                    return s;
                })
                .toList();

        detalle.put("historial", historialSimplificado);

        return detalle;
    }


    public String marcarSesionCompletada(Long miembroId, String observaciones) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        Optional<AsignacionRutina> asignacionOpt = asignacionRutinaRepository
                .findByMiembroAndActivoTrue(miembro);

        if (!asignacionOpt.isPresent()) {
            return "ERROR: El miembro no tiene una rutina asignada";
        }

        try {
            AsignacionRutina asignacion = asignacionOpt.get();

            // ✅ SIEMPRE crea una nueva sesión, sin importar si ya hay una hoy
            SesionCompletada sesion = new SesionCompletada(asignacion, miembro, observaciones);
            sesionCompletadaRepository.save(sesion);

            return "SUCCESS: Sesión completada registrada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo registrar la sesión - " + e.getMessage();
        }
    }


    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> stats = new HashMap<>();

        // Total de miembros con rutinas asignadas
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();
        long miembrosConRutina = miembrosActivos.stream()
                .filter(m -> asignacionRutinaRepository.existsByMiembroAndActivoTrue(m))
                .count();

        stats.put("miembrosConRutina", miembrosConRutina);

        // Sesiones completadas hoy
        LocalDate hoy = LocalDate.now();
        long sesionesHoy = sesionCompletadaRepository.countByFechaCompletada(hoy);
        stats.put("sesionesHoy", sesionesHoy);

        // Sesiones este mes
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        long sesionesMes = sesionCompletadaRepository
                .countByFechaCompletadaBetween(inicioMes, finMes);
        stats.put("sesionesMes", sesionesMes);

        return stats;
    }


    public String eliminarSesion(Long sesionId) {
        Optional<SesionCompletada> sesionOpt = sesionCompletadaRepository.findById(sesionId);

        if (!sesionOpt.isPresent()) {
            return "ERROR: Sesión no encontrada";
        }

        try {
            sesionCompletadaRepository.deleteById(sesionId);
            return "SUCCESS: Sesión eliminada correctamente";
        } catch (Exception e) {
            return "ERROR: No se pudo eliminar la sesión";
        }
    }
}
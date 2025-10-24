package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.*;
import com.example.ProyectoGym.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio para la gestion del progreso de entrenamiento de los miembros.
 * Proporciona funcionalidades de seguimiento de rutinas, registro de sesiones completadas,
 * estadisticas de progreso y visualizacion del cumplimiento de objetivos.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class ProgresoService {

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private AsignacionRutinaRepository asignacionRutinaRepository;

    @Autowired
    private SesionCompletadaRepository sesionCompletadaRepository;

    /**
     * Obtiene la lista de miembros activos que tienen rutinas asignadas con su progreso mensual.
     * Incluye informacion de sesiones completadas, meta mensual y porcentaje de cumplimiento.
     * Los resultados se ordenan por porcentaje de progreso descendente.
     *
     * @return Lista de mapas con informacion de cada miembro y su progreso de entrenamiento
     */
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

                        Long sesionesEsteMes = sesionCompletadaRepository
                                .countSesionesEsteMes(miembro);

                        int metaMensual = rutina.getFrecuenciaSemanal() * 4;
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

    /**
     * Obtiene el detalle completo del progreso de un miembro especifico.
     * Incluye informacion del miembro, rutina asignada, estadisticas de sesiones,
     * porcentaje de progreso mensual y historial de las ultimas 10 sesiones.
     *
     * @param miembroId ID del miembro a consultar
     * @return Mapa con informacion detallada del progreso, o null si el miembro no existe o no tiene rutina
     */
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

        detalle.put("miembroId", miembro.getId());
        detalle.put("miembroNombre", miembro.getNombre());
        detalle.put("miembroDni", miembro.getDni());
        detalle.put("miembroEmail", miembro.getEmail());

        detalle.put("rutinaNombre", rutina.getNombre());
        detalle.put("rutinaDescripcion", rutina.getDescripcion());
        detalle.put("objetivo", asignacion.getObjetivoSeleccionado());
        detalle.put("nivel", asignacion.getNivelSeleccionado());
        detalle.put("duracion", rutina.getDuracion());
        detalle.put("frecuenciaSemanal", rutina.getFrecuenciaSemanal());
        detalle.put("fechaAsignacion", asignacion.getFechaAsignacion());

        Long sesionesEsteMes = sesionCompletadaRepository.countSesionesEsteMes(miembro);
        Long totalSesiones = sesionCompletadaRepository.countByMiembro(miembro);
        int metaMensual = rutina.getFrecuenciaSemanal() * 4;
        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);

        detalle.put("sesionesEsteMes", sesionesEsteMes);
        detalle.put("totalSesiones", totalSesiones);
        detalle.put("metaMensual", metaMensual);
        detalle.put("porcentajeProgreso", Math.min(porcentaje, 100));
        detalle.put("sesionesFaltantes", Math.max(0, metaMensual - sesionesEsteMes));

        SesionCompletada ultimaSesion = sesionCompletadaRepository
                .findFirstByMiembroOrderByFechaCompletadaDesc(miembro);

        if (ultimaSesion != null) {
            detalle.put("ultimaSesion", ultimaSesion.getFechaCompletada());
        } else {
            detalle.put("ultimaSesion", null);
        }

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

    /**
     * Registra la completacion de una sesion de entrenamiento para un miembro.
     * Valida que el miembro exista y tenga una rutina asignada activa.
     * Siempre crea una nueva sesion sin importar si ya hay una registrada en el dia.
     *
     * @param miembroId ID del miembro que completo la sesion
     * @param observaciones Notas u observaciones sobre la sesion (opcional)
     * @return Mensaje de exito o error segun corresponda
     */
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

            SesionCompletada sesion = new SesionCompletada(asignacion, miembro, observaciones);
            sesionCompletadaRepository.save(sesion);

            return "SUCCESS: Sesión completada registrada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo registrar la sesión - " + e.getMessage();
        }
    }

    /**
     * Obtiene estadisticas generales del sistema de seguimiento de progreso.
     * Incluye cantidad de miembros con rutinas asignadas, sesiones del dia y del mes.
     *
     * @return Mapa con estadisticas generales del gimnasio
     */
    public Map<String, Object> obtenerEstadisticasGenerales() {
        Map<String, Object> stats = new HashMap<>();

        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();
        long miembrosConRutina = miembrosActivos.stream()
                .filter(m -> asignacionRutinaRepository.existsByMiembroAndActivoTrue(m))
                .count();

        stats.put("miembrosConRutina", miembrosConRutina);

        LocalDate hoy = LocalDate.now();
        long sesionesHoy = sesionCompletadaRepository.countByFechaCompletada(hoy);
        stats.put("sesionesHoy", sesionesHoy);

        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        long sesionesMes = sesionCompletadaRepository
                .countByFechaCompletadaBetween(inicioMes, finMes);
        stats.put("sesionesMes", sesionesMes);

        return stats;
    }

    /**
     * Elimina una sesion completada del sistema.
     * Util para corregir registros erroneos.
     *
     * @param sesionId ID de la sesion a eliminar
     * @return Mensaje de exito o error segun corresponda
     */
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
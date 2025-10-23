package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.*;
import com.example.ProyectoGym.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RutinaService {

    @Autowired
    private RutinaPredefinidaRepository rutinaPredefinidaRepository;

    @Autowired
    private EjercicioRutinaRepository ejercicioRutinaRepository;

    @Autowired
    private AsignacionRutinaRepository asignacionRutinaRepository;

    @Autowired
    private SesionCompletadaRepository sesionCompletadaRepository;


    // Verificar si el miembro ya tiene una rutina asignada
    public boolean tieneRutinaAsignada(Miembro miembro) {
        return asignacionRutinaRepository.existsByMiembroAndActivoTrue(miembro);
    }

    // Obtener rutina asignada del miembro
    public AsignacionRutina obtenerRutinaAsignada(Miembro miembro) {
        Optional<AsignacionRutina> asignacion = asignacionRutinaRepository.findByMiembroAndActivoTrue(miembro);
        return asignacion.orElse(null);
    }

    // Asignar rutina a un miembro según objetivo y nivel seleccionado
    public String asignarRutina(Miembro miembro, String objetivo, String nivel) {
        // Verificar si ya tiene una rutina asignada
        if (tieneRutinaAsignada(miembro)) {
            return "ERROR: Ya tienes una rutina asignada";
        }

        // Buscar la rutina predefinida según objetivo y nivel
        Optional<RutinaPredefinida> rutinaOpt = rutinaPredefinidaRepository
                .findByObjetivoAndNivelAndActivoTrue(objetivo, nivel);

        if (!rutinaOpt.isPresent()) {
            return "ERROR: No se encontró una rutina para ese objetivo y nivel";
        }

        try {
            RutinaPredefinida rutina = rutinaOpt.get();
            AsignacionRutina asignacion = new AsignacionRutina(miembro, rutina, objetivo, nivel);
            asignacionRutinaRepository.save(asignacion);
            return "SUCCESS: Rutina asignada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo asignar la rutina";
        }
    }

    // Obtener ejercicios de la rutina asignada
    public List<EjercicioRutina> obtenerEjerciciosDeRutina(RutinaPredefinida rutina) {
        return ejercicioRutinaRepository.findByRutinaPredefinidaOrderByOrdenAsc(rutina);
    }

    // Obtener estadísticas del miembro
    public Long contarSesionesEsteMes(Miembro miembro) {
        return sesionCompletadaRepository.countSesionesEsteMes(miembro);
    }

    public Long contarSesionesTotales(Miembro miembro) {
        return sesionCompletadaRepository.countByMiembro(miembro);
    }

    public SesionCompletada obtenerUltimaSesion(Miembro miembro) {
        return sesionCompletadaRepository.findFirstByMiembroOrderByFechaCompletadaDesc(miembro);
    }

    // Obtener historial de sesiones
    public List<SesionCompletada> obtenerHistorialSesiones(Miembro miembro) {
        return sesionCompletadaRepository.findByMiembroOrderByFechaCompletadaDesc(miembro);
    }

    // Calcular porcentaje de progreso (sesiones este mes vs frecuencia semanal)
    public int calcularPorcentajeProgreso(Miembro miembro) {
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);
        if (asignacion == null) {
            return 0;
        }

        Long sesionesEsteMes = contarSesionesEsteMes(miembro);
        Integer frecuenciaSemanal = asignacion.getRutinaPredefinida().getFrecuenciaSemanal();

        if (frecuenciaSemanal == null || frecuenciaSemanal == 0) {
            return 0;
        }

        // Meta mensual = frecuencia semanal * 4 semanas
        int metaMensual = frecuenciaSemanal * 4;

        if (metaMensual == 0) {
            return 0;
        }

        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);
        return Math.min(porcentaje, 100); // No más de 100%
    }

    // Calcular sesiones restantes para completar la meta mensual
    public int calcularSesionesRestantes(Miembro miembro) {
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);
        if (asignacion == null) {
            return 0;
        }

        Long sesionesEsteMes = contarSesionesEsteMes(miembro);
        Integer frecuenciaSemanal = asignacion.getRutinaPredefinida().getFrecuenciaSemanal();

        if (frecuenciaSemanal == null) {
            return 0;
        }

        int metaMensual = frecuenciaSemanal * 4;
        int restantes = metaMensual - sesionesEsteMes.intValue();

        return Math.max(restantes, 0); // No puede ser negativo
    }


    // Registrar sesión completada
    public String registrarSesionCompletada(Miembro miembro, String observaciones) {
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);

        if (asignacion == null) {
            return "ERROR: El miembro no tiene una rutina asignada";
        }

        try {
            SesionCompletada sesion = new SesionCompletada(asignacion, miembro, observaciones);
            sesionCompletadaRepository.save(sesion);
            return "SUCCESS: Sesión registrada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo registrar la sesión";
        }
    }

    // Obtener todas las rutinas predefinidas
    public List<RutinaPredefinida> obtenerTodasLasRutinas() {
        return rutinaPredefinidaRepository.findByActivoTrue();
    }

    // Crear rutina predefinida
    public String crearRutinaPredefinida(String nombre, String descripcion, String objetivo,
                                         String nivel, Integer duracion, Integer frecuenciaSemanal) {
        try {
            RutinaPredefinida rutina = new RutinaPredefinida(nombre, descripcion, objetivo,
                    nivel, duracion, frecuenciaSemanal);
            rutinaPredefinidaRepository.save(rutina);
            return "SUCCESS: Rutina creada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo crear la rutina";
        }
    }

    // Cancelar rutina actual
    public String cancelarRutinaActual(Miembro miembro) {
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);

        if (asignacion == null) {
            return "ERROR: No tienes una rutina asignada";
        }

        try {
            asignacion.setActivo(false);
            asignacionRutinaRepository.save(asignacion);
            return "SUCCESS: Rutina cancelada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo cancelar la rutina";
        }
    }
    // Calcular porcentaje de asistencia este mes
    public int calcularPorcentajeAsistencia(Miembro miembro) {
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);
        if (asignacion == null) {
            return 0;
        }

        Long sesionesEsteMes = contarSesionesEsteMes(miembro);
        Integer frecuenciaSemanal = asignacion.getRutinaPredefinida().getFrecuenciaSemanal();

        if (frecuenciaSemanal == null || frecuenciaSemanal == 0) {
            return 0;
        }

        int metaMensual = frecuenciaSemanal * 4;

        if (metaMensual == 0) {
            return 0;
        }

        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);
        return Math.min(porcentaje, 100);
    }

    // Obtener sesiones del mes anterior
    public Long contarSesionesMesAnterior(Miembro miembro) {
        return sesionCompletadaRepository.countSesionesMesAnterior(miembro);
    }

    // Calcular diferencia con mes anterior
    public int calcularDiferenciaMesAnterior(Miembro miembro) {
        Long sesionesEsteMes = contarSesionesEsteMes(miembro);
        Long sesionesMesAnterior = contarSesionesMesAnterior(miembro);

        if (sesionesMesAnterior == 0) {
            return 0;
        }

        long diferencia = sesionesEsteMes - sesionesMesAnterior;
        return (int) ((diferencia * 100.0) / sesionesMesAnterior);
    }

    // Calcular próxima sesión recomendada
    public LocalDate calcularProximaSesion(Miembro miembro) {
        SesionCompletada ultimaSesion = obtenerUltimaSesion(miembro);
        AsignacionRutina asignacion = obtenerRutinaAsignada(miembro);

        if (ultimaSesion == null || asignacion == null) {
            return LocalDate.now();
        }

        Integer frecuenciaSemanal = asignacion.getRutinaPredefinida().getFrecuenciaSemanal();
        if (frecuenciaSemanal == null || frecuenciaSemanal == 0) {
            return LocalDate.now();
        }

        // Calcular días entre sesiones
        int diasEntreSesiones = 7 / frecuenciaSemanal;

        return ultimaSesion.getFechaCompletada().plusDays(diasEntreSesiones);
    }

    // Obtener últimas N sesiones
    public List<SesionCompletada> obtenerUltimasSesiones(Miembro miembro, int limite) {
        List<SesionCompletada> todas = obtenerHistorialSesiones(miembro);
        if (todas.size() <= limite) {
            return todas;
        }
        return todas.subList(0, limite);
    }
}

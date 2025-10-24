package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.*;
import com.example.ProyectoGym.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestion de rutinas de entrenamiento y asignaciones a miembros.
 * Proporciona funcionalidades de asignacion de rutinas, seguimiento de sesiones,
 * calculo de estadisticas de progreso y recomendaciones personalizadas.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
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

    /**
     * Verifica si un miembro tiene una rutina activa asignada.
     *
     * @param miembro Miembro a verificar
     * @return true si tiene rutina activa, false en caso contrario
     */
    public boolean tieneRutinaAsignada(Miembro miembro) {
        return asignacionRutinaRepository.existsByMiembroAndActivoTrue(miembro);
    }

    /**
     * Obtiene la rutina actualmente asignada a un miembro.
     *
     * @param miembro Miembro del cual obtener la rutina
     * @return La asignacion de rutina activa o null si no tiene ninguna
     */
    public AsignacionRutina obtenerRutinaAsignada(Miembro miembro) {
        Optional<AsignacionRutina> asignacion = asignacionRutinaRepository.findByMiembroAndActivoTrue(miembro);
        return asignacion.orElse(null);
    }

    /**
     * Asigna una rutina predefinida a un miembro segun objetivo y nivel seleccionado.
     * Valida que el miembro no tenga una rutina activa previamente y que exista
     * una rutina predefinida que coincida con los criterios.
     *
     * @param miembro Miembro al cual asignar la rutina
     * @param objetivo Objetivo de entrenamiento ("Bajar Peso", "Tonificar", "Aumentar Masa Muscular")
     * @param nivel Nivel de dificultad ("Principiante", "Intermedio", "Avanzado")
     * @return Mensaje de exito o error segun corresponda
     */
    public String asignarRutina(Miembro miembro, String objetivo, String nivel) {
        if (tieneRutinaAsignada(miembro)) {
            return "ERROR: Ya tienes una rutina asignada";
        }

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

    /**
     * Obtiene la lista de ejercicios de una rutina predefinida ordenados por secuencia.
     *
     * @param rutina Rutina de la cual obtener los ejercicios
     * @return Lista de ejercicios ordenados por el campo orden
     */
    public List<EjercicioRutina> obtenerEjerciciosDeRutina(RutinaPredefinida rutina) {
        return ejercicioRutinaRepository.findByRutinaPredefinidaOrderByOrdenAsc(rutina);
    }

    /**
     * Cuenta las sesiones completadas por un miembro en el mes actual.
     *
     * @param miembro Miembro del cual contar sesiones
     * @return Cantidad de sesiones completadas en el mes
     */
    public Long contarSesionesEsteMes(Miembro miembro) {
        return sesionCompletadaRepository.countSesionesEsteMes(miembro);
    }

    /**
     * Cuenta el total de sesiones completadas por un miembro desde su registro.
     *
     * @param miembro Miembro del cual contar sesiones totales
     * @return Cantidad total de sesiones completadas
     */
    public Long contarSesionesTotales(Miembro miembro) {
        return sesionCompletadaRepository.countByMiembro(miembro);
    }

    /**
     * Obtiene la ultima sesion completada por un miembro.
     *
     * @param miembro Miembro del cual obtener la ultima sesion
     * @return La sesion mas reciente o null si no tiene sesiones
     */
    public SesionCompletada obtenerUltimaSesion(Miembro miembro) {
        return sesionCompletadaRepository.findFirstByMiembroOrderByFechaCompletadaDesc(miembro);
    }

    /**
     * Obtiene el historial completo de sesiones de un miembro ordenadas por fecha descendente.
     *
     * @param miembro Miembro del cual obtener el historial
     * @return Lista de sesiones ordenadas de mas reciente a mas antigua
     */
    public List<SesionCompletada> obtenerHistorialSesiones(Miembro miembro) {
        return sesionCompletadaRepository.findByMiembroOrderByFechaCompletadaDesc(miembro);
    }

    /**
     * Calcula el porcentaje de progreso del miembro en el mes actual.
     * Compara las sesiones completadas contra la meta mensual basada en frecuencia semanal.
     *
     * @param miembro Miembro del cual calcular el progreso
     * @return Porcentaje de cumplimiento (0-100), 0 si no tiene rutina asignada
     */
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

        int metaMensual = frecuenciaSemanal * 4;

        if (metaMensual == 0) {
            return 0;
        }

        int porcentaje = (int) ((sesionesEsteMes * 100.0) / metaMensual);
        return Math.min(porcentaje, 100);
    }

    /**
     * Calcula cuantas sesiones faltan para completar la meta mensual.
     *
     * @param miembro Miembro del cual calcular sesiones restantes
     * @return Cantidad de sesiones faltantes, 0 si ya completo la meta o no tiene rutina
     */
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

        return Math.max(restantes, 0);
    }

    /**
     * Registra una nueva sesion completada por un miembro.
     * Valida que el miembro tenga una rutina asignada activa.
     *
     * @param miembro Miembro que completo la sesion
     * @param observaciones Notas u observaciones sobre la sesion
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Obtiene todas las rutinas predefinidas activas disponibles.
     *
     * @return Lista de rutinas activas
     */
    public List<RutinaPredefinida> obtenerTodasLasRutinas() {
        return rutinaPredefinidaRepository.findByActivoTrue();
    }

    /**
     * Crea una nueva rutina predefinida en el sistema.
     *
     * @param nombre Nombre de la rutina
     * @param descripcion Descripcion detallada
     * @param objetivo Objetivo de la rutina
     * @param nivel Nivel de dificultad
     * @param duracion Duracion en semanas
     * @param frecuenciaSemanal Numero de sesiones por semana
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Cancela la rutina actualmente asignada a un miembro marcandola como inactiva.
     *
     * @param miembro Miembro cuya rutina se cancelara
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Calcula el porcentaje de asistencia del miembro en el mes actual.
     * Equivalente a calcularPorcentajeProgreso.
     *
     * @param miembro Miembro del cual calcular asistencia
     * @return Porcentaje de asistencia (0-100)
     */
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

    /**
     * Cuenta las sesiones completadas por un miembro en el mes anterior.
     *
     * @param miembro Miembro del cual contar sesiones
     * @return Cantidad de sesiones del mes anterior
     */
    public Long contarSesionesMesAnterior(Miembro miembro) {
        return sesionCompletadaRepository.countSesionesMesAnterior(miembro);
    }

    /**
     * Calcula la diferencia porcentual de sesiones entre el mes actual y el anterior.
     * Util para medir mejora o disminucion en la constancia del miembro.
     *
     * @param miembro Miembro del cual calcular diferencia
     * @return Porcentaje de diferencia (positivo si mejoro, negativo si disminuyo)
     */
    public int calcularDiferenciaMesAnterior(Miembro miembro) {
        Long sesionesEsteMes = contarSesionesEsteMes(miembro);
        Long sesionesMesAnterior = contarSesionesMesAnterior(miembro);

        if (sesionesMesAnterior == 0) {
            return 0;
        }

        long diferencia = sesionesEsteMes - sesionesMesAnterior;
        return (int) ((diferencia * 100.0) / sesionesMesAnterior);
    }

    /**
     * Calcula la proxima fecha recomendada para entrenar basandose en la frecuencia semanal.
     * Toma la fecha de la ultima sesion y suma los dias entre sesiones.
     *
     * @param miembro Miembro del cual calcular proxima sesion
     * @return Fecha recomendada para la proxima sesion
     */
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

        int diasEntreSesiones = 7 / frecuenciaSemanal;

        return ultimaSesion.getFechaCompletada().plusDays(diasEntreSesiones);
    }

    /**
     * Obtiene las ultimas N sesiones completadas por un miembro.
     *
     * @param miembro Miembro del cual obtener sesiones
     * @param limite Cantidad maxima de sesiones a retornar
     * @return Lista de las ultimas sesiones limitada al numero especificado
     */
    public List<SesionCompletada> obtenerUltimasSesiones(Miembro miembro, int limite) {
        List<SesionCompletada> todas = obtenerHistorialSesiones(miembro);
        if (todas.size() <= limite) {
            return todas;
        }
        return todas.subList(0, limite);
    }
}
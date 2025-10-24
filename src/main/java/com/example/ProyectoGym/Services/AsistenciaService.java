package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Asistencia;
import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Repository.AsistenciaRepository;
import com.example.ProyectoGym.Repository.MiembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestion de asistencias al gimnasio.
 * Proporciona funcionalidades de registro de entradas y salidas, consultas de asistencia
 * y generacion de reportes de presencia de miembros.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private MiembroRepository miembroRepository;

    // Formateador para mostrar horas sin segundos
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Registra la entrada de un miembro al gimnasio.
     * Valida que el miembro exista, este activo, no tenga membresia vencida
     * y no tenga una entrada activa previa sin salida registrada.
     *
     * @param miembroId ID del miembro que ingresa
     * @return Mensaje de exito con hora de entrada o mensaje de error segun corresponda
     */
    public String registrarEntrada(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        if (!miembro.getActivo() || miembro.estaVencida()) {
            return "ERROR: Membresía inactiva o vencida. Debe renovar.";
        }

        if (asistenciaRepository.miembroEstaEnGimnasio(miembro)) {
            return "ERROR: El miembro ya tiene una entrada activa";
        }

        Asistencia asistencia = new Asistencia(miembro);
        asistenciaRepository.save(asistencia);

        return "SUCCESS: Entrada registrada a las " +
                asistencia.getFechaHoraEntrada().toLocalTime().format(TIME_FORMATTER);
    }

    /**
     * Registra la salida de un miembro del gimnasio.
     * Busca la asistencia activa del miembro y registra la hora de salida,
     * calculando automaticamente la duracion de la visita.
     *
     * @param miembroId ID del miembro que sale
     * @return Mensaje de exito con duracion de la visita o mensaje de error si no hay entrada registrada
     */
    public String registrarSalida(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        Optional<Asistencia> asistenciaOpt = asistenciaRepository.findAsistenciaActivaByMiembro(miembro);

        if (!asistenciaOpt.isPresent()) {
            return "ERROR: No hay entrada registrada para este miembro";
        }

        Asistencia asistencia = asistenciaOpt.get();
        asistencia.registrarSalida();
        asistenciaRepository.save(asistencia);

        return "SUCCESS: Salida registrada. Duración: " +
                asistencia.getDuracionMinutos() + " minutos";
    }

    /**
     * Obtiene todas las asistencias registradas en el dia actual.
     *
     * @return Lista de asistencias del dia
     */
    public List<Asistencia> obtenerAsistenciasHoy() {
        return asistenciaRepository.findAsistenciasHoy();
    }

    /**
     * Obtiene la lista de miembros que estan actualmente en el gimnasio.
     * Retorna asistencias sin salida registrada.
     *
     * @return Lista de asistencias activas (miembros presentes)
     */
    public List<Asistencia> obtenerMiembrosEnGimnasio() {
        return asistenciaRepository.findMiembrosEnGimnasio();
    }

    /**
     * Verifica si un miembro especifico esta actualmente en el gimnasio.
     *
     * @param miembroId ID del miembro a verificar
     * @return true si el miembro esta en el gimnasio, false en caso contrario
     */
    public boolean miembroEstaEnGimnasio(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return false;
        return asistenciaRepository.miembroEstaEnGimnasio(miembro);
    }

    /**
     * Obtiene el historial completo de asistencias de un miembro especifico.
     * Las asistencias se retornan ordenadas de mas reciente a mas antigua.
     *
     * @param miembroId ID del miembro
     * @return Lista de asistencias del miembro
     */
    public List<Asistencia> obtenerAsistenciasPorMiembro(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return List.of();
        return asistenciaRepository.findByMiembroOrderByFechaHoraEntradaDesc(miembro);
    }

    /**
     * Obtiene las asistencias registradas dentro de un rango de fechas.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de asistencias en el periodo especificado
     */
    public List<Asistencia> obtenerAsistenciasPorFecha(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        return asistenciaRepository.findAsistenciasBetween(inicioDateTime, finDateTime);
    }

    /**
     * Cuenta el numero total de asistencias registradas en el dia actual.
     *
     * @return Cantidad de asistencias del dia
     */
    public long contarAsistenciasHoy() {
        return asistenciaRepository.countAsistenciasHoy();
    }

    /**
     * Cuenta cuantos miembros estan actualmente en el gimnasio.
     *
     * @return Cantidad de miembros presentes
     */
    public long contarMiembrosEnGimnasio() {
        return asistenciaRepository.countMiembrosEnGimnasio();
    }

    /**
     * Obtiene todas las asistencias del mes actual.
     *
     * @return Lista de asistencias del mes
     */
    public List<Asistencia> obtenerAsistenciasMesActual() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();
        return asistenciaRepository.findByMesAndAnio(mes, anio);
    }

    /**
     * Cuenta las asistencias de un miembro especifico en el mes actual.
     * Util para verificar el uso del plan de membresia.
     *
     * @param miembroId ID del miembro
     * @return Cantidad de asistencias del miembro en el mes
     */
    public long contarAsistenciasMiembroMes(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return 0;
        return asistenciaRepository.countAsistenciasMiembroMesActual(miembro);
    }

    /**
     * Genera una lista de todos los miembros activos con su estado de asistencia del dia.
     * Incluye informacion de presencia, hora de entrada y si esta actualmente en el gimnasio.
     *
     * @return Lista de mapas con informacion de cada miembro y su estado de asistencia
     */
    public List<Map<String, Object>> obtenerListaMiembrosConEstado() {
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();
        List<Asistencia> asistenciasHoy = asistenciaRepository.findAsistenciasHoy();

        return miembrosActivos.stream().map(miembro -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", miembro.getId());
            info.put("nombre", miembro.getNombre());
            info.put("dni", miembro.getDni());
            info.put("plan", miembro.getPlan());

            Optional<Asistencia> asistenciaOpt = asistenciasHoy.stream()
                    .filter(a -> a.getMiembro().getId().equals(miembro.getId()) && a.estaEnGimnasio())
                    .findFirst();

            if (asistenciaOpt.isPresent()) {
                Asistencia asistencia = asistenciaOpt.get();
                info.put("estado", "Presente");
                info.put("horaEntrada", asistencia.getFechaHoraEntrada().toLocalTime().format(TIME_FORMATTER));
                info.put("enGimnasio", true);
            } else {
                boolean yaSalio = asistenciasHoy.stream()
                        .anyMatch(a -> a.getMiembro().getId().equals(miembro.getId()) && !a.estaEnGimnasio());

                if (yaSalio) {
                    info.put("estado", "Asistió");
                    info.put("horaEntrada", "----");
                    info.put("enGimnasio", false);
                } else {
                    info.put("estado", "Ausente");
                    info.put("horaEntrada", "----");
                    info.put("enGimnasio", false);
                }
            }

            return info;
        }).toList();
    }

    /**
     * Genera un historial detallado de todas las asistencias del dia.
     * Incluye informacion de entrada, salida, duracion y estado de cada visita.
     *
     * @return Lista de mapas con detalles de cada asistencia del dia
     */
    public List<Map<String, Object>> obtenerHistorialHoy() {
        List<Asistencia> asistencias = asistenciaRepository.findAsistenciasHoy();

        return asistencias.stream().map(asistencia -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", asistencia.getId());
            info.put("nombre", asistencia.getMiembro().getNombre());
            info.put("dni", asistencia.getMiembro().getDni());
            info.put("horaEntrada", asistencia.getFechaHoraEntrada().toLocalTime().format(TIME_FORMATTER));

            if (asistencia.getFechaHoraSalida() != null) {
                info.put("horaSalida", asistencia.getFechaHoraSalida().toLocalTime().format(TIME_FORMATTER));
                info.put("duracion", asistencia.getDuracionMinutos() + " min");
                info.put("tipo", "Completa");
            } else {
                info.put("horaSalida", "----");
                info.put("duracion", "En gimnasio");
                info.put("tipo", "En curso");
            }

            return info;
        }).toList();
    }
}
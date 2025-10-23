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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private MiembroRepository miembroRepository;


    public String registrarEntrada(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        // Verificar si el miembro está activo y no vencido
        if (!miembro.getActivo() || miembro.estaVencida()) {
            return "ERROR: Membresía inactiva o vencida. Debe renovar.";
        }

        // Verificar si ya tiene una entrada activa (sin salida)
        if (asistenciaRepository.miembroEstaEnGimnasio(miembro)) {
            return "ERROR: El miembro ya tiene una entrada activa";
        }

        // Crear nueva asistencia
        Asistencia asistencia = new Asistencia(miembro);
        asistenciaRepository.save(asistencia);

        return "SUCCESS: Entrada registrada a las " +
                asistencia.getFechaHoraEntrada().toLocalTime().toString();
    }


    public String registrarSalida(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        // Buscar asistencia activa
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


    // Obtener todas las asistencias de hoy
    public List<Asistencia> obtenerAsistenciasHoy() {
        return asistenciaRepository.findAsistenciasHoy();
    }

    // Obtener miembros actualmente en el gimnasio
    public List<Asistencia> obtenerMiembrosEnGimnasio() {
        return asistenciaRepository.findMiembrosEnGimnasio();
    }

    // Verificar si un miembro está en el gimnasio
    public boolean miembroEstaEnGimnasio(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return false;
        return asistenciaRepository.miembroEstaEnGimnasio(miembro);
    }

    // Obtener asistencias de un miembro
    public List<Asistencia> obtenerAsistenciasPorMiembro(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return List.of();
        return asistenciaRepository.findByMiembroOrderByFechaHoraEntradaDesc(miembro);
    }

    // Obtener asistencias entre fechas
    public List<Asistencia> obtenerAsistenciasPorFecha(LocalDate inicio, LocalDate fin) {
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        return asistenciaRepository.findAsistenciasBetween(inicioDateTime, finDateTime);
    }



    // Contar asistencias de hoy
    public long contarAsistenciasHoy() {
        return asistenciaRepository.countAsistenciasHoy();
    }

    // Contar miembros actualmente en el gimnasio
    public long contarMiembrosEnGimnasio() {
        return asistenciaRepository.countMiembrosEnGimnasio();
    }

    // Obtener asistencias del mes actual
    public List<Asistencia> obtenerAsistenciasMesActual() {
        LocalDate hoy = LocalDate.now();
        int mes = hoy.getMonthValue();
        int anio = hoy.getYear();
        return asistenciaRepository.findByMesAndAnio(mes, anio);
    }

    // Contar asistencias de un miembro en el mes
    public long contarAsistenciasMiembroMes(Long miembroId) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);
        if (miembro == null) return 0;
        return asistenciaRepository.countAsistenciasMiembroMesActual(miembro);
    }



    // Obtener lista de todos los miembros con su estado de asistencia hoy
    public List<Map<String, Object>> obtenerListaMiembrosConEstado() {
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();
        List<Asistencia> asistenciasHoy = asistenciaRepository.findAsistenciasHoy();

        return miembrosActivos.stream().map(miembro -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", miembro.getId());
            info.put("nombre", miembro.getNombre());
            info.put("dni", miembro.getDni());
            info.put("plan", miembro.getPlan());

            // Buscar si tiene asistencia hoy
            Optional<Asistencia> asistenciaOpt = asistenciasHoy.stream()
                    .filter(a -> a.getMiembro().getId().equals(miembro.getId()) && a.estaEnGimnasio())
                    .findFirst();

            if (asistenciaOpt.isPresent()) {
                Asistencia asistencia = asistenciaOpt.get();
                info.put("estado", "Presente");
                info.put("horaEntrada", asistencia.getFechaHoraEntrada().toLocalTime().toString());
                info.put("enGimnasio", true);
            } else {
                // Verificar si ya salió
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

    // Obtener historial de asistencias de hoy con detalles
    public List<Map<String, Object>> obtenerHistorialHoy() {
        List<Asistencia> asistencias = asistenciaRepository.findAsistenciasHoy();

        return asistencias.stream().map(asistencia -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", asistencia.getId());
            info.put("nombre", asistencia.getMiembro().getNombre());
            info.put("dni", asistencia.getMiembro().getDni());
            info.put("horaEntrada", asistencia.getFechaHoraEntrada().toLocalTime().toString());

            if (asistencia.getFechaHoraSalida() != null) {
                info.put("horaSalida", asistencia.getFechaHoraSalida().toLocalTime().toString());
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

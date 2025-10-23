package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private ClaseGrupalRepository claseGrupalRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private AsignacionRutinaRepository asignacionRutinaRepository;

    @Autowired
    private SesionCompletadaRepository sesionCompletadaRepository;


    // Contar miembros activos
    public long obtenerMiembrosActivos() {
        return miembroRepository.findMiembrosActivos().size();
    }

    // Contar membresías activas (Premium + Básico activos)
    public long obtenerMembresiasActivas() {
        return miembroRepository.findMiembrosActivos().size();
    }

    // Contar asistencias del día (sesiones completadas hoy)
    public long obtenerAsistenciasHoy() {
        LocalDate hoy = LocalDate.now();
        return sesionCompletadaRepository.countByFechaCompletada(hoy);
    }

    // Calcular ingresos del mes
    public BigDecimal calcularIngresosMes() {
        // Obtener TODOS los miembros activos
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();

        BigDecimal ingresoTotal = BigDecimal.ZERO;

        for (Miembro miembro : miembrosActivos) {
            // Primero intentar obtener el precio del planDetalle
            if (miembro.getPlanDetalle() != null && miembro.getPlanDetalle().getPrecio() != null) {
                ingresoTotal = ingresoTotal.add(miembro.getPlanDetalle().getPrecio());
            }
            // Si no tiene planDetalle, buscar el plan por nombre
            else if (miembro.getPlan() != null && !miembro.getPlan().isEmpty()) {
                Plan plan = planRepository.findByNombre(miembro.getPlan()).orElse(null);
                if (plan != null && plan.getPrecio() != null) {
                    ingresoTotal = ingresoTotal.add(plan.getPrecio());
                }
            }
        }

        return ingresoTotal;
    }

    // Obtener distribución de planes
    public Map<String, Long> obtenerDistribucionPlanes() {
        Map<String, Long> distribucion = new HashMap<>();

        // Obtener todos los miembros activos
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();

        // Contar cuántos tienen cada plan
        long countBasico = miembrosActivos.stream()
                .filter(m -> m.getPlan() != null &&
                        (m.getPlan().equalsIgnoreCase("Basico") ||
                                m.getPlan().equalsIgnoreCase("BASICO")))
                .count();

        long countPremium = miembrosActivos.stream()
                .filter(m -> m.getPlan() != null &&
                        (m.getPlan().equalsIgnoreCase("Premium") ||
                                m.getPlan().equalsIgnoreCase("PREMIUM")))
                .count();

        distribucion.put("Basico", countBasico);
        distribucion.put("Premium", countPremium);

        return distribucion;
    }

    // Obtener porcentajes de planes para gráfico
    public Map<String, Integer> obtenerPorcentajesPlanes() {
        Map<String, Long> distribucion = obtenerDistribucionPlanes();
        long total = distribucion.get("Basico") + distribucion.get("Premium");

        Map<String, Integer> porcentajes = new HashMap<>();

        if (total > 0) {
            int porcentajeBasico = (int) ((distribucion.get("Basico") * 100) / total);
            int porcentajePremium = (int) ((distribucion.get("Premium") * 100) / total);

            porcentajes.put("Basico", porcentajeBasico);
            porcentajes.put("Premium", porcentajePremium);
        } else {
            porcentajes.put("Basico", 0);
            porcentajes.put("Premium", 0);
        }

        return porcentajes;
    }


    // Obtener todas las clases con información resumida
    public List<Map<String, Object>> obtenerResumenClases() {
        List<com.example.ProyectoGym.Model.ClaseGrupal> clases = claseGrupalRepository.findAll();

        return clases.stream().map(clase -> {
            Map<String, Object> resumen = new HashMap<>();
            resumen.put("id", clase.getId());
            resumen.put("nombre", clase.getNombre());
            resumen.put("instructor", clase.getInstructor() != null ?
                    clase.getInstructor().getNombre() : "Sin asignar");
            resumen.put("diaSemana", clase.getDiaSemana());
            resumen.put("horaInicio", clase.getHoraInicio());
            resumen.put("inscritos", reservaRepository.countReservasActivasByClase(clase));
            resumen.put("capacidad", clase.getCapacidad());
            return resumen;
        }).toList();
    }


    // Contar total de miembros
    public long obtenerTotalMiembros() {
        return miembroRepository.count();
    }

    // Contar total de clases
    public long obtenerTotalClases() {
        return claseGrupalRepository.count();
    }

    // Contar total de instructores
    public long obtenerTotalInstructores() {
        return instructorRepository.count();
    }

    // Contar reservas activas totales
    public long obtenerReservasActivas() {
        return reservaRepository.countByEstado("ACTIVA");
    }

    // Obtener miembros próximos a vencer (próximos 7 días)
    public List<Miembro> obtenerMiembrosProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate dentroDe7Dias = hoy.plusDays(7);
        return miembroRepository.findByFechaVencimientoBetween(hoy, dentroDe7Dias);
    }

    // Obtener miembros vencidos
    public List<Miembro> obtenerMiembrosVencidos() {
        LocalDate hoy = LocalDate.now();
        return miembroRepository.findByFechaVencimientoBefore(hoy);
    }


    // Obtener reporte mensual
    public Map<String, Object> obtenerReporteMensual() {
        Map<String, Object> reporte = new HashMap<>();

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // Nuevos miembros este mes
        List<Miembro> nuevosMiembros = miembroRepository
                .findByFechaRegistroBetween(inicioMes, finMes);
        reporte.put("nuevosMiembros", nuevosMiembros.size());

        // Ingresos del mes
        reporte.put("ingresosMes", calcularIngresosMes());

        // Miembros activos
        reporte.put("miembrosActivos", obtenerMiembrosActivos());

        // Asistencias del mes
        long asistenciasMes = sesionCompletadaRepository
                .countByFechaCompletadaBetween(inicioMes, finMes);
        reporte.put("asistenciasMes", asistenciasMes);

        // Clases con más reservas
        reporte.put("clasesPopulares", obtenerClasesPopulares(5));

        return reporte;
    }

    // Obtener clases más populares
    public List<Map<String, Object>> obtenerClasesPopulares(int limite) {
        List<com.example.ProyectoGym.Model.ClaseGrupal> clases = claseGrupalRepository
                .findByActivaTrue();

        return clases.stream()
                .map(clase -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("nombre", clase.getNombre());
                    info.put("reservas", reservaRepository.countReservasActivasByClase(clase));
                    return info;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("reservas"),
                        (Long) a.get("reservas")))
                .limit(limite)
                .toList();
    }


    // Obtener todas las métricas del dashboard en un solo metodo
    public Map<String, Object> obtenerMetricasDashboard() {
        Map<String, Object> metricas = new HashMap<>();

        // Contadores principales
        metricas.put("miembrosActivos", obtenerMiembrosActivos());
        metricas.put("membresiasActivas", obtenerMembresiasActivas());
        metricas.put("asistenciasHoy", obtenerAsistenciasHoy());
        metricas.put("ingresosMes", calcularIngresosMes());

        // Distribución de planes
        metricas.put("distribucionPlanes", obtenerDistribucionPlanes());
        metricas.put("porcentajesPlanes", obtenerPorcentajesPlanes());

        // Resumen de clases
        metricas.put("resumenClases", obtenerResumenClases());

        // Alertas
        metricas.put("miembrosProximosAVencer", obtenerMiembrosProximosAVencer().size());
        metricas.put("miembrosVencidos", obtenerMiembrosVencidos().size());

        return metricas;
    }
}
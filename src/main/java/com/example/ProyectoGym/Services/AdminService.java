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

/**
 * Servicio para la gestion administrativa del gimnasio.
 * Proporciona metricas, estadisticas, reportes y datos del dashboard para administradores.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
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

    /**
     * Obtiene el numero de miembros activos en el gimnasio.
     *
     * @return Cantidad de miembros con estado activo
     */
    public long obtenerMiembrosActivos() {
        return miembroRepository.findMiembrosActivos().size();
    }

    /**
     * Obtiene el numero de membresias activas en el gimnasio.
     * Incluye tanto planes Premium como Basico activos.
     *
     * @return Cantidad de membresias activas
     */
    public long obtenerMembresiasActivas() {
        return miembroRepository.findMiembrosActivos().size();
    }

    /**
     * Cuenta las asistencias registradas en el dia actual.
     * Se basa en sesiones completadas en la fecha de hoy.
     *
     * @return Cantidad de asistencias del dia
     */
    public long obtenerAsistenciasHoy() {
        LocalDate hoy = LocalDate.now();
        return sesionCompletadaRepository.countByFechaCompletada(hoy);
    }

    /**
     * Calcula los ingresos totales del mes actual.
     * Suma los precios de los planes de todos los miembros activos.
     * Prioriza el precio del planDetalle, y si no existe, busca el plan por nombre.
     *
     * @return Ingresos totales del mes como BigDecimal
     */
    public BigDecimal calcularIngresosMes() {
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();
        BigDecimal ingresoTotal = BigDecimal.ZERO;

        for (Miembro miembro : miembrosActivos) {
            if (miembro.getPlanDetalle() != null && miembro.getPlanDetalle().getPrecio() != null) {
                ingresoTotal = ingresoTotal.add(miembro.getPlanDetalle().getPrecio());
            }
            else if (miembro.getPlan() != null && !miembro.getPlan().isEmpty()) {
                Plan plan = planRepository.findByNombre(miembro.getPlan()).orElse(null);
                if (plan != null && plan.getPrecio() != null) {
                    ingresoTotal = ingresoTotal.add(plan.getPrecio());
                }
            }
        }

        return ingresoTotal;
    }

    /**
     * Obtiene la distribucion de miembros por tipo de plan.
     * Cuenta cuantos miembros activos tienen plan Basico y cuantos Premium.
     *
     * @return Mapa con la cantidad de miembros por plan (Basico, Premium)
     */
    public Map<String, Long> obtenerDistribucionPlanes() {
        Map<String, Long> distribucion = new HashMap<>();
        List<Miembro> miembrosActivos = miembroRepository.findMiembrosActivos();

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

    /**
     * Calcula los porcentajes de distribucion de planes para graficos.
     * Convierte la cantidad de miembros por plan en porcentajes sobre el total.
     *
     * @return Mapa con porcentajes de cada plan (Basico, Premium)
     */
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

    /**
     * Obtiene un resumen de todas las clases grupales con informacion relevante.
     * Incluye nombre, instructor, horario, cantidad de inscritos y capacidad.
     *
     * @return Lista de mapas con informacion resumida de cada clase
     */
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

    /**
     * Obtiene el numero total de miembros registrados en el gimnasio.
     *
     * @return Cantidad total de miembros (activos e inactivos)
     */
    public long obtenerTotalMiembros() {
        return miembroRepository.count();
    }

    /**
     * Obtiene el numero total de clases grupales registradas.
     *
     * @return Cantidad total de clases
     */
    public long obtenerTotalClases() {
        return claseGrupalRepository.count();
    }

    /**
     * Obtiene el numero total de instructores registrados.
     *
     * @return Cantidad total de instructores
     */
    public long obtenerTotalInstructores() {
        return instructorRepository.count();
    }

    /**
     * Cuenta el numero total de reservas activas en el sistema.
     *
     * @return Cantidad de reservas con estado ACTIVA
     */
    public long obtenerReservasActivas() {
        return reservaRepository.countByEstado("ACTIVA");
    }

    /**
     * Obtiene la lista de miembros cuya membresia vence en los proximos 7 dias.
     * Util para enviar recordatorios de renovacion.
     *
     * @return Lista de miembros proximos a vencer
     */
    public List<Miembro> obtenerMiembrosProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate dentroDe7Dias = hoy.plusDays(7);
        return miembroRepository.findByFechaVencimientoBetween(hoy, dentroDe7Dias);
    }

    /**
     * Obtiene la lista de miembros cuya membresia ya ha vencido.
     *
     * @return Lista de miembros con membresia vencida
     */
    public List<Miembro> obtenerMiembrosVencidos() {
        LocalDate hoy = LocalDate.now();
        return miembroRepository.findByFechaVencimientoBefore(hoy);
    }

    /**
     * Genera un reporte mensual completo con estadisticas del gimnasio.
     * Incluye nuevos miembros, ingresos, asistencias y clases populares del mes.
     *
     * @return Mapa con metricas mensuales
     */
    public Map<String, Object> obtenerReporteMensual() {
        Map<String, Object> reporte = new HashMap<>();

        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Miembro> nuevosMiembros = miembroRepository
                .findByFechaRegistroBetween(inicioMes, finMes);
        reporte.put("nuevosMiembros", nuevosMiembros.size());

        reporte.put("ingresosMes", calcularIngresosMes());
        reporte.put("miembrosActivos", obtenerMiembrosActivos());

        long asistenciasMes = sesionCompletadaRepository
                .countByFechaCompletadaBetween(inicioMes, finMes);
        reporte.put("asistenciasMes", asistenciasMes);

        reporte.put("clasesPopulares", obtenerClasesPopulares(5));

        return reporte;
    }

    /**
     * Obtiene las clases mas populares ordenadas por numero de reservas.
     *
     * @param limite Cantidad maxima de clases a retornar
     * @return Lista de clases con su cantidad de reservas, ordenadas descendentemente
     */
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

    /**
     * Consolida todas las metricas del dashboard en un unico metodo.
     * Proporciona contadores principales, distribucion de planes, resumen de clases y alertas.
     *
     * @return Mapa con todas las metricas necesarias para el dashboard administrativo
     */
    public Map<String, Object> obtenerMetricasDashboard() {
        Map<String, Object> metricas = new HashMap<>();

        metricas.put("miembrosActivos", obtenerMiembrosActivos());
        metricas.put("membresiasActivas", obtenerMembresiasActivas());
        metricas.put("asistenciasHoy", obtenerAsistenciasHoy());
        metricas.put("ingresosMes", calcularIngresosMes());

        metricas.put("distribucionPlanes", obtenerDistribucionPlanes());
        metricas.put("porcentajesPlanes", obtenerPorcentajesPlanes());

        metricas.put("resumenClases", obtenerResumenClases());

        metricas.put("miembrosProximosAVencer", obtenerMiembrosProximosAVencer().size());
        metricas.put("miembrosVencidos", obtenerMiembrosVencidos().size());

        return metricas;
    }
}
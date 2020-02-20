package com.prosesol.springboot.app.controller;

import com.prosesol.springboot.app.entity.Beneficio;
import com.prosesol.springboot.app.entity.CentroContacto;
import com.prosesol.springboot.app.entity.Servicio;
import com.prosesol.springboot.app.entity.rel.RelServicioBeneficio;
import com.prosesol.springboot.app.service.*;
import mx.openpay.client.Plan;
import mx.openpay.client.core.OpenpayAPI;
import mx.openpay.client.enums.PlanRepeatUnit;
import mx.openpay.client.enums.PlanStatusAfterRetry;
import mx.openpay.client.exceptions.OpenpayServiceException;
import mx.openpay.client.exceptions.ServiceUnavailableException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("servicio")
@RequestMapping("/servicios")
public class ServicioController {

    protected final Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    private IServicioService servicioService;

    @Autowired
    private IBeneficioService beneficioService;

    @Autowired
    private ICentroContactoService centroContactoService;

    @Autowired
    private IRelServicioBeneficioService relServicioBeneficioService;

    @Autowired
    private IPlanService planService;

    @Value("${openpay.pk}")
    private String privateKey;

    @Value("${openpay.url}")
    private String openpayURL;

    @Value("${openpay.id}")
    private String merchantId;

    private Long idServicioGeneral;

    /**
     * Método para la creación de un Servicio
     *
     * @param model
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/crear")
    public String crear(Map<String, Object> model) {

        Servicio servicio = new Servicio();
        boolean titular = false;
        boolean beneficiario = false;

        model.put("servicio", servicio);
        model.put("titulo", "Crear Servicio");
        model.put("titular", titular);
        model.put("beneficiario", beneficiario);

        logger.info("Id servicio desde el método de crear: " + servicio.getId());

        return "catalogos/servicios/crear";

    }

    /**
     * Método para la visualización del Servicio
     *
     * @param model
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/ver", method = RequestMethod.GET)
    public String ver(Model model) {

        try {
            model.addAttribute("titulo", "Membresía");
            model.addAttribute("servicios", servicioService.findAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "catalogos/servicios/ver";
    }

    /**
     * Método para el detalle del Servicio
     *
     * @param id
     * @param model
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/detalle/{id}")
    public String detalle(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes redirect) {

        Servicio servicio = null;
        List<RelServicioBeneficio> listaServicioBeneficio = relServicioBeneficioService
                .getRelServicioBeneficioByIdServicio(id);

        List<Beneficio> beneficios = new ArrayList<Beneficio>();

        if (id > 0) {
            servicio = servicioService.findById(id);

            if (servicio == null) {
                redirect.addFlashAttribute("error", "El servicio no existe en la base de datos");
                return "redirect:/servicios/ver";
            }
        } else {
            redirect.addFlashAttribute("error", "El id del servicio no puede ser cero");
            return "redirect:/servicios/ver";
        }

        for (RelServicioBeneficio listabeneficio : listaServicioBeneficio) {
            Beneficio beneficio = new Beneficio();

            beneficio = beneficioService.findById(listabeneficio.getBeneficio().getId());
            beneficios.add(beneficio);
        }

        model.put("servicio", servicio);
        model.put("listaBeneficios", beneficios);

        return "catalogos/servicios/detalle";

    }

    /**
     * Almacena el servicio con sus beneficios dentro de la base de datos
     * Verifica si el servicio se cnvertirá en plan
     *
     * @param servicio
     * @param result
     * @param redirect
     * @param status
     * @param idBeneficio
     * @param descripcion
     * @param beneficiario
     * @param titular
     * @param beneDescripcion
     * @param isPlan
     * @return
     * @throws Exception
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/crear", method = RequestMethod.POST, params = "action=save")
    public String guardar(@Valid Servicio servicio, BindingResult result, RedirectAttributes redirect,
                          SessionStatus status, @RequestParam(name = "beneficio[]", required = false) List<Long> idBeneficio,
                          @RequestParam(name = "descripcion[]", required = false) List<String> descripcion,
                          @RequestParam(name = "beneficiario[]", required = false) List<Long> beneficiario,
                          @RequestParam(name = "titular[]", required = false) List<Long> titular,
                          @RequestParam(name = "beneDescripcion[]", required = false) List<Long> beneDescripcion,
                          @RequestParam(value = "isPlan", required = false) String isPlan,
                          Model model) {

        logger.info("Entra al método para guardar o modificar el servicio");

        String flashMessage = "";
        RelServicioBeneficio relServicioBeneficio = new RelServicioBeneficio();

        try {

            if (result.hasErrors()) {
                redirect.addFlashAttribute("error", "Campos incompletos");
                return "catalogos/servicios/crear";
            }

            // Verifica si se necesita editar el servicio o se deberá de crear

            if (servicio.getId() != null) {

                servicio.setEstatus(true);
                servicioService.save(servicio);

                // Verifica si el servicio se editará con todo y beneficios

                if (idBeneficio != null && idBeneficio.size() > 0) {
                    editarServiciosConBeneficios(servicio, idBeneficio, descripcion, titular, beneficiario);
                }

                flashMessage = "Servicio editado correctamente";

            } else {

                // Verifica si el servicio se creará con todo y beneficios

                if (idBeneficio != null && idBeneficio.size() > 0) {

                    //descripcion.removeAll(Arrays.asList(" ",""));

                    servicio.setEstatus(true);
                    servicioService.save(servicio);

                    int dIndex = 0;
                    int tIndex = 0;
                    int bIndex = 0;
                    int countTitular = 1;
                    int countBeneficiario = 1;
                    String guardoBeneficiario = null;
                    String guardoTitular = null;
                    ArrayList<String> lB = new ArrayList<String>();
                    ArrayList<String> lT = new ArrayList<String>();
                    for (Long beneficio : idBeneficio) {

                        Beneficio nBeneficio = beneficioService.findById(beneficio);

                        relServicioBeneficio.setServicio(servicio);
                        relServicioBeneficio.setBeneficio(nBeneficio);

                        if (titular != null && titular.size() >= countTitular) {
                            if (beneficio == titular.get(tIndex)) {
                                relServicioBeneficio.setTitular(true);
                                relServicioBeneficio.setBeneficiario(false);
                                for (Long idbeneficio : beneDescripcion) {
                                    if (idbeneficio == beneficio) {
                                        if (descripcion.get(dIndex) == " ") {
                                            relServicioBeneficio.setDescripcion(descripcion.get(dIndex).trim());
                                        } else {
                                            relServicioBeneficio.setDescripcion(descripcion.get(dIndex).trim());
                                        }

                                        break;
                                    }
                                    dIndex++;
                                }

                            } else if (beneficio != titular.get(tIndex)) {
                                relServicioBeneficio.setTitular(false);
                                guardoTitular = titular.get(tIndex).toString();
                                lT.add(guardoTitular);
                            }
                            dIndex = 0;
                            tIndex++;
                            countTitular++;
                        }

                        if (beneficiario != null && beneficiario.size() >= countBeneficiario) {
                            if (beneficio == beneficiario.get(bIndex)) {
                                for (Long idbeneficio : beneDescripcion) {
                                    if (idbeneficio == beneficio) {
                                        if (descripcion.get(dIndex) == " ") {
                                            relServicioBeneficio.setDescripcion(descripcion.get(dIndex).trim());
                                        } else {
                                            relServicioBeneficio.setDescripcion(descripcion.get(dIndex).trim());
                                        }

                                        break;
                                    }
                                    dIndex++;
                                }

                                if (titular == null) {
                                    relServicioBeneficio.setBeneficiario(true);
                                } else {
                                    if (beneficiario.get(bIndex) == titular.get(tIndex - 1)) {
                                        relServicioBeneficio.setBeneficiario(true);
                                    } else {
                                        relServicioBeneficio.setTitular(false);
                                        relServicioBeneficio.setBeneficiario(true);
                                    }
                                }

                                bIndex++;
                                countBeneficiario++;
                            } else {
                                relServicioBeneficio.setBeneficiario(false);
                                guardoBeneficiario = beneficiario.get(bIndex).toString();
                                lB.add(guardoBeneficiario);
                                bIndex++;
                                countBeneficiario++;
                            }
                            dIndex = 0;
                        }
                        if (!lB.isEmpty()) {
                            for (int x = 0; x < lB.size(); x++) {
                                if (lB.get(x).equals(beneficio.toString()) && !lB.get(x).equals(titular.get(tIndex - 1).toString())) {
                                    relServicioBeneficio.setTitular(false);
                                    relServicioBeneficio.setBeneficiario(true);
                                    lB.remove(x);
                                    guardoBeneficiario = null;
                                    break;
                                }


                                if (lB.get(x).equals(titular.get(tIndex - 1).toString())) {
                                    relServicioBeneficio.setBeneficiario(true);
                                    relServicioBeneficio.setTitular(true);
                                    lB.remove(x);
                                    guardoBeneficiario = null;
                                    break;
                                }
                            }
                        }

                        if (!lT.isEmpty()) {
                            for (int x = 0; x < lT.size(); x++) {
                                if (lT.get(x).equals(beneficio.toString())) {
                                    relServicioBeneficio.setTitular(true);
                                    lT.remove(x);
                                    guardoTitular = null;
                                    break;
                                }


                            }
                        }
                        relServicioBeneficioService.save(relServicioBeneficio);

                    }

                    flashMessage = "Servicio creado correctamente";

                } else { // Solamente se inserta el servicio

                    servicio.setEstatus(true);

                    // Se creará un plan por el servicio si se selecciona el checkbox isValid

                    if (isPlan != null) {
                        if (servicio.getCostoTitular() > 0 ||
                                servicio.getCostoBeneficiario() > 0) {
                            servicioService.save(servicio);
                            guardarPlan(servicio);
                        } else {
                            model.addAttribute("error", "El servicio no tiene costo para " +
                                    "poder agregarlo como plan");
                            return "/catalogos/servicios/crear";
                        }

                    } else {
                        servicioService.save(servicio);
                    }

                    flashMessage = "Servicio creado correctamente";
                }

            }

            status.setComplete();
            redirect.addFlashAttribute("success", flashMessage);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info("Id servicio desde el método de guardar: " + servicio.getId());

        return "redirect:/servicios/ver";

    }

    /**
     * Método para la edición del Servicio
     *
     * @param idServicio
     * @param model
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/editar/{id}")
    public String editar(@PathVariable(value = "id") Long idServicio, Model model, RedirectAttributes redirect) {

        Servicio servicio = null;
        List<RelServicioBeneficio> relServicioBeneficios = getRelServicioBeneficioByIdServicio(idServicio);
        List<Beneficio> beneficios = beneficioService.findAll();
        List<RelServicioBeneficio> nRelServicioBeneficio = new ArrayList<RelServicioBeneficio>();

        try {
            if (idServicio > 0) {
                servicio = servicioService.findById(idServicio);
                if (servicio == null) {
                    redirect.addFlashAttribute("error", "El id del servicio no existe");
                    return "redirect:/servicios/ver";
                }

                int countSB = 0;

                for (Beneficio beneficio : beneficios) {
                    if (relServicioBeneficios.size() > countSB
                            && beneficio.getId() == relServicioBeneficios.get(countSB).getBeneficio().getId()) {

                        RelServicioBeneficio relServicioBeneficio = new RelServicioBeneficio(
                                relServicioBeneficios.get(countSB).getServicio(),
                                relServicioBeneficios.get(countSB).getBeneficio(),
                                relServicioBeneficios.get(countSB).getTitular(),
                                relServicioBeneficios.get(countSB).getBeneficiario(),
                                relServicioBeneficios.get(countSB).getDescripcion());

                        nRelServicioBeneficio.add(relServicioBeneficio);

                        countSB++;
                    } else {

                        RelServicioBeneficio relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, false,
                                false, null);

                        nRelServicioBeneficio.add(relServicioBeneficio);
                    }

                }

            } else {
                redirect.addFlashAttribute("error", "El id del servicio no puede ser cero");
                return "redirect:/servicios/ver";
            }
        } catch (Exception ex) {
            redirect.addFlashAttribute("error", "Ocurrió un error en el sistema, contacte al administrador");
            ex.printStackTrace();
            return "redirect:/servicios/ver";
        }

        idServicioGeneral = idServicio;

        model.addAttribute("servicio", servicio);
        model.addAttribute("relServicioBeneficios", nRelServicioBeneficio);

        return "catalogos/servicios/editar";

    }

    /**
     * Método para borrar el Servicio
     *
     * @param id
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/eliminar/{id}")
    public String borrar(@PathVariable(value = "id") Long id, RedirectAttributes redirect) {

        try {
            if (id > 0) {
                servicioService.delete(id);
                redirect.addFlashAttribute("success", "Registro eliminado correctamente");
            }
        } catch (Exception e) {
            logger.error("Ocurrió un error al momento de eliminar el registro", e);
            redirect.addFlashAttribute("error", "El servicio no se puede eliminar porque está asignado a un Afiliado");
        }

        return "redirect:/servicios/ver";
    }

    /**
     * Método para borrar los beneficios en una lista
     *
     * @param beneficios
     * @param model
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/crear", method = RequestMethod.POST, params = "action=delete")
    public String borrarBeneficios(@RequestParam(name = "beneficio[]", required = false) List<Long> beneficios,
                                   Model model, RedirectAttributes redirect) {
        try {

            for (Long beneficio : beneficios) {
                relServicioBeneficioService.removeBeneficiobyIdBeneficio(beneficio);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Ocurrió un error al momento de realizar la eliminación de los beneficios", e);
            return "redirect:/servicios/ver";
        }

        redirect.addFlashAttribute("success", "Beneficios eliminados correctamente");

        return "redirect:/servicios/ver/";
    }

    /**
     * Cátalogo de beneficios para la vista de creación de servicios
     *
     * @return
     */

    @ModelAttribute("beneficios")
    public List<Beneficio> getAllBeneficios() {
        return beneficioService.findAll();
    }

    /**
     * Método que obtiene la lista de los beneficios que pertenecen a cada Servicio
     *
     * @param idServicio
     * @return
     */

    public List<RelServicioBeneficio> getRelServicioBeneficioByIdServicio(Long idServicio) {

        List<RelServicioBeneficio> relServicioBeneficio = relServicioBeneficioService
                .getRelServicioBeneficioByIdServicio(idServicio);

        return relServicioBeneficio;
    }

    /**
     * Método que obtiene la lista de los centros de asistencia por cada servicio
     *
     * @return
     */

    @ModelAttribute("centros")
    public List<CentroContacto> getAllCentroContacto() {

        List<CentroContacto> centrosContacto = centroContactoService.findAll();

        return centrosContacto;

    }

    /**
     * Método que edita o inserta beneficios
     *
     * @param servicio
     * @param idBeneficio
     * @param descripcion
     * @param titular'
     * @param beneficiario
     */

    public void editarServiciosConBeneficios(Servicio servicio, List<Long> idBeneficio, List<String> descripcion,
                                             List<Long> titular, List<Long> beneficiario) {

        RelServicioBeneficio relServicioBeneficio = null;
        List<Beneficio> beneficios = beneficioService.findAll();
        Map<Long, String> beneficioDescripcion = new HashMap<Long, String>();

        int countDescripcion = 0;

        for (Beneficio b : beneficios) {
            beneficioDescripcion.put(b.getId(), descripcion.get(countDescripcion));
            countDescripcion++;
        }

        for (Long id : idBeneficio) {
            Beneficio beneficio = beneficioService.findById(id);

            boolean isTitular = false;
            boolean isBeneficiario = false;
            if (titular != null && titular.size() > 0) {
                for (Long idTitular : titular) {
                    if (id == idTitular) {
                        for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                            if (entry.getKey() == id) {
                                relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, true, false, entry.getValue());
                                break;
                            }
                        }

                        isTitular = true;
                        break;
                    }
                    if (id != idTitular) {
                        for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                            if (entry.getKey() == id) {
                                relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, false, false, entry.getValue());
                                break;
                            }
                        }
                    }
                }
            } else {
                for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                    if (entry.getKey() == id) {
                        relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, false, false, entry.getValue());
                        break;
                    }
                }
            }
            if (beneficiario != null && beneficiario.size() > 0) {
                for (Long idBeneficiario : beneficiario) {
                    if (id == idBeneficiario) {
                        for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                            if (entry.getKey() == id) {
                                relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, false, true, entry.getValue());
                                break;
                            }
                        }
                        isBeneficiario = true;
                        break;
                    }
                    if (id != idBeneficiario) {
                        for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                            System.out.println("id: " + id);
                            System.out.println("entry: " + entry.getKey());
                            if (entry.getKey() == id) {
                                relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, false, false, entry.getValue());
                                break;
                            }
                        }
                    }
                }
            }

            if (isTitular && isBeneficiario) {
                for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                    if (entry.getKey() == id) {
                        relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, true, true, entry.getValue());
                        break;
                    }
                }
            }

            if (isTitular && !isBeneficiario) {
                for (Map.Entry<Long, String> entry : beneficioDescripcion.entrySet()) {
                    if (entry.getKey() == id) {
                        relServicioBeneficio = new RelServicioBeneficio(servicio, beneficio, true, false, entry.getValue());
                        break;
                    }
                }
            }
            relServicioBeneficioService.save(relServicioBeneficio);
        }
    }

    @ModelAttribute("listaTipoPrivacidad")
    public Map getTipoPrivacidad() {
        Map<Boolean, String> tipoPrivacidad = new HashMap<Boolean, String>();

        tipoPrivacidad.put(true, "Privado");
        tipoPrivacidad.put(false, "Público");

        return tipoPrivacidad;
    }

    /**
     * Se guarda el servicio como plan
     *
     * @param servicio
     */

    private void guardarPlan(Servicio servicio) throws ServiceUnavailableException, OpenpayServiceException {

        OpenpayAPI api = new OpenpayAPI(openpayURL, privateKey, merchantId);
        Plan plan = new Plan();
        Double totalServicio = 0.0;

        plan.name(servicio.getNombre());
        if (servicio.getCostoTitular() > 0) {
        	if(servicio.getCostoBeneficiario() > 0){
				totalServicio = servicio.getCostoTitular() + servicio.getCostoBeneficiario();
				plan.amount(BigDecimal.valueOf(totalServicio));
			}else{
				plan.amount(BigDecimal.valueOf(servicio.getCostoTitular()));
			}
        }  else if (servicio.getCostoBeneficiario() > 0) {
            plan.amount(BigDecimal.valueOf(servicio.getCostoBeneficiario()));
        }
        plan.repeatEvery(1, PlanRepeatUnit.MONTH);
        plan.retryTimes(3);
        plan.statusAfterRetry(PlanStatusAfterRetry.UNPAID);
        plan.trialDays(30);

        plan = api.plans().create(plan);

        if (plan.getStatus().equals("active")) {
            logger.info("Activada");
            com.prosesol.springboot.app.entity.Plan planProsesol =
                    new com.prosesol.springboot.app.entity.Plan(servicio, servicio.getNombre(),
                            plan.getId());

            planService.save(planProsesol);
        }
    }

}

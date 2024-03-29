package com.prosesol.springboot.app.controller;

import com.prosesol.springboot.app.async.AsyncCargaMasiva;
import com.prosesol.springboot.app.async.AsyncCargaMoneygram;
import com.prosesol.springboot.app.async.AsyncCargaVigor;
import com.prosesol.springboot.app.entity.Cuenta;
import com.prosesol.springboot.app.entity.LogCM;
import com.prosesol.springboot.app.exception.CustomValidatorExcelException;
import com.prosesol.springboot.app.service.IAfiliadoService;
import com.prosesol.springboot.app.service.ICuentaService;
import com.prosesol.springboot.app.service.ILogCMService;
import com.prosesol.springboot.app.view.excel.ReportesExcelImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

import java.io.OutputStream;
import java.util.List;

@Controller
@RequestMapping("/cargaMasiva")
public class CargaMasivaController {

    protected static final Log logger = LogFactory.getLog(CargaMasivaController.class);

    @Autowired
    private ReportesExcelImpl reportesExcelImpl;

    @Autowired
    private AsyncCargaMasiva asyncCargaMasiva;

    @Autowired
    private AsyncCargaVigor asyncCargaVigor;
    
    @Autowired
    private AsyncCargaMoneygram asyncCargaMoneygram;

    @Autowired
    private ILogCMService logCMService;

    @Autowired
    private IAfiliadoService afiliadoService;

    @Autowired
    private ICuentaService cuentaService;

    /**
     * Vista de la tabla de carga masiva de afiliados
     * @param model
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping("/afiliados")
    public String cargaMasiva(Model model, RedirectAttributes redirect) {

        try{
            model.addAttribute("reportes", logCMService.findAllLogsAfiliados());
        }catch(Exception e){
            e.printStackTrace();
            redirect.addFlashAttribute("error: ", "Error al momento de generar" +
                    "la tabla");
            return "redirect:/cargaMasiva/reportes";
        }

        return "cargaMasiva/afiliados";
    }

    /**
     * Vista de la Carga Masiva del Archivo Vigor
     * @param model
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping("/vigor")
    public String cargaMasivaVigor(Model model, RedirectAttributes redirect){

        try{

            model.addAttribute("reportes", logCMService.findAllLogsVigor());

        }catch(Exception e){
            e.printStackTrace();
            redirect.addFlashAttribute("error: ", "Error al momento de generar" +
                    "la tabla");
            return "redirect:/cargaMasiva/reportes";
        }

        return "cargaMasiva/vigor";
    }
    
    /**
     * Vista de la Carga Masiva afiliado moneygram
     * @param model
     * @param redirect
     * @return
     */
    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping("/moneygram")
    public String cargaMasivaMoneygram(Model model, RedirectAttributes redirect){

        try{

            model.addAttribute("reportes", logCMService.findAllLogsMoneygram());

        }catch(Exception e){
            e.printStackTrace();
            redirect.addFlashAttribute("error: ", "Error al momento de generar" +
                    "la tabla");
            return "redirect:/cargaMasiva/reportes";
        }

        return "cargaMasiva/moneygram";
    }

    /**
     * Descarga el archivo tanto para afiliados como para vigor
     * @param response
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping("/templateXlsx")
    public String descargarTemplate(HttpServletResponse response, RedirectAttributes redirect) {

        try {

            reportesExcelImpl.generarTemplateAfiliadoXlsx(response);

        } catch (CustomValidatorExcelException e) {

            redirect.addFlashAttribute("error", e.getMessage());
            logger.error("Error: " , e);
            return "redirect:/cargaMasiva/afiliados";

        }

        return null;
    }
    
    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping("/templateMoneygramXlsx")
    public String descargarTemplateMoneygram(HttpServletResponse response, RedirectAttributes redirect) {

        try {

            reportesExcelImpl.generarTemplateAfiliadoMoneygramXlsx(response);

        } catch (CustomValidatorExcelException e) {

            redirect.addFlashAttribute("error", e.getMessage());
            logger.error("Error: " , e);
            return "redirect:/cargaMasiva/moneygram";

        }

        return null;
    }

    /**
     * Carga el archivo de carga masivo de afiliados
     * @param file
     * @param response
     * @param redirect
     * @return
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadCargaMasiva(@RequestParam("file") MultipartFile file, HttpServletResponse response,
							 RedirectAttributes redirect) {

        boolean isVigor = false;
        boolean isConciliacion = false;
        try {

        	  if(!file.isEmpty()){

        	      int indexOfName = file.getOriginalFilename().indexOf(".");
        	      String extension = file.getOriginalFilename().substring(file.getOriginalFilename()
                          .lastIndexOf(".") + 1);
        	      String nombreArchivo = null;

                  if(!extension.equals("csv")){
                      redirect.addFlashAttribute("error",
                              "El archivo no es de tipo CSV, favor de convertir" +
                                      " el archivo de tipo CSV");
                      return "redirect:/cargaMasiva/afiliados";
                  }

        	      if(indexOfName != -1){
        	          nombreArchivo = file.getOriginalFilename().substring(0, indexOfName);
                  }

        	      logger.info("File Size: " + file.getBytes().length);
        	      logger.info("File Type: " + file.getContentType());
        	      logger.info("File Name: " + file.getOriginalFilename());
        	      
        	      byte[] bytes = new String(file.getBytes(),"ISO-8859-1").getBytes("UTF-8"); 
    			  asyncCargaMasiva.procesaArchivoAsync(isVigor,isConciliacion, nombreArchivo, bytes, null);
        	    	 	 
			  }else{
        	      redirect.addFlashAttribute("warning", "Por favor, seleccione" +
                          " un archivo");
        	      return "redirect:/cargaMasiva/afiliados";
              }

        } catch (Exception ne) {

            logger.error("Formato incorrecto", ne);
            redirect.addFlashAttribute("error", "Error al momento de realizar la inserción masiva");

            return "redirect:/cargaMasiva/afiliados";
        }

        redirect.addFlashAttribute("info", "El archivo se está procesando");
        return "redirect:/afiliados/ver";

    }

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/upload/vigor", method = RequestMethod.POST)
    public String uploadCargaVigor(@ModelAttribute("cuenta")Cuenta cuenta,
                                   @RequestParam("file") MultipartFile file,
                                   HttpServletResponse response, RedirectAttributes redirect) {

        boolean isVigor = true;
        boolean isConciliacion=false;

        try {

            if(!file.isEmpty()){

                afiliadoService.updateEstatusbyIdCuenta(cuenta.getId());


                int indexOfName = file.getOriginalFilename().indexOf(".");
                String extension = file.getOriginalFilename().substring(file.getOriginalFilename()
                        .lastIndexOf(".") + 1);
                String nombreArchivo = null;

                if(indexOfName != -1){
                    nombreArchivo = file.getOriginalFilename().substring(0, indexOfName);
                }

                if(!extension.equals("csv")){
                    redirect.addFlashAttribute("error",
                            "El archivo no es de tipo CSV, favor de convertir" +
                                    " el archivo de tipo CSV");
                    return "redirect:/cargaMasiva/vigor";
                }

                if(indexOfName != -1){
                    nombreArchivo = file.getOriginalFilename().substring(0, indexOfName);
                }

                logger.info("File Size: " + file.getBytes().length);
                logger.info("File Type: " + file.getContentType());
                logger.info("File Name: " + file.getOriginalFilename());

                byte[] bytes = new String(file.getBytes(),"ISO-8859-1").getBytes("UTF-8");
                asyncCargaVigor.procesaArchivoVigorAsync(isVigor,isConciliacion,
                nombreArchivo, bytes, cuenta.getId());


            }else{
                redirect.addFlashAttribute("warning", "Por favor, seleccione" +
                        " un archivo");
                return "redirect:/cargaMasiva/vigor";
            }

        }catch (Exception ne){
            logger.error("Formato incorrecto", ne);
            redirect.addFlashAttribute("error", "Error al momento de realizar la inserción masiva");

            return "redirect:/cargaMasiva/vigor";
        }

        redirect.addFlashAttribute("info", "El archivo se está procesando");
        return "redirect:/afiliados/ver";

    }
    
    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @RequestMapping(value = "/upload/moneygram", method = RequestMethod.POST)
    public String uploadCargaMoneygram(@ModelAttribute("cuenta")Cuenta cuenta,
                                   @RequestParam("file") MultipartFile file,
                                   HttpServletResponse response, RedirectAttributes redirect) {

        boolean isVigor = false;
        boolean isConciliacion = false;
        boolean isMoneygram = true;

        try {

            if(!file.isEmpty()){

                afiliadoService.updateEstatusbyIdCuenta(cuenta.getId());


                int indexOfName = file.getOriginalFilename().indexOf(".");
                String extension = file.getOriginalFilename().substring(file.getOriginalFilename()
                        .lastIndexOf(".") + 1);
                String nombreArchivo = null;

                if(indexOfName != -1){
                    nombreArchivo = file.getOriginalFilename().substring(0, indexOfName);
                }

                if(!extension.equals("csv")){
                    redirect.addFlashAttribute("error",
                            "El archivo no es de tipo CSV, favor de convertir" +
                                    " el archivo de tipo CSV");
                    return "redirect:/cargaMasiva/vigor";
                }

                if(indexOfName != -1){
                    nombreArchivo = file.getOriginalFilename().substring(0, indexOfName);
                }

                logger.info("File Size: " + file.getBytes().length);
                logger.info("File Type: " + file.getContentType());
                logger.info("File Name: " + file.getOriginalFilename());

                byte[] bytes = new String(file.getBytes(),"ISO-8859-1").getBytes("UTF-8");
                asyncCargaMoneygram.procesaArchivoMoneygramAsync(isVigor,isConciliacion,isMoneygram,
                nombreArchivo, bytes);
	

            }else{
                redirect.addFlashAttribute("warning", "Por favor, seleccione" +
                        " un archivo");
                return "redirect:/cargaMasiva/moneygram";
            }

        }catch (Exception ne){
            logger.error("Formato incorrecto", ne);
            redirect.addFlashAttribute("error", "Error al momento de realizar la inserción masiva");

            return "redirect:/cargaMasiva/moneygram";
        }

        redirect.addFlashAttribute("info", "El archivo se está procesando");
        return "redirect:/afiliados/ver";

    }


    /**
     * Descarga el reporte tanto para afiliados como para vigor
     * @param id
     * @param response
     */

    @Secured({"ROLE_ADMINISTRADOR", "ROLE_USUARIO"})
    @GetMapping(value = "/reportes/{id}")
    public void downloadReporte(@PathVariable("id") Long id, HttpServletResponse response){

        LogCM logCM = logCMService.findById(id);

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=" + logCM.getNombre());

        byte[] bytes = logCM.getArchivo();

        try{
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la lista de las cuentas en la BBDD
     * @return List<Cuenta>
     */
    @ModelAttribute("cuentas")
    public List<Cuenta> getAllCuentas(){
        return cuentaService.findAll();
    }
}

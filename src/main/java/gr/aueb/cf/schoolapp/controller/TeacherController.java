package gr.aueb.cf.schoolapp.controller;
import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.RegionReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherEditDTO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.dto.TeacherReadOnlyDTO;
import gr.aueb.cf.schoolapp.model.Teacher;
import gr.aueb.cf.schoolapp.service.IRegionService;
import gr.aueb.cf.schoolapp.service.ITeacherService;
import gr.aueb.cf.schoolapp.validator.TeacherInsertValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final ITeacherService teacherService;
    private final IRegionService regionService;
    private final TeacherInsertValidator teacherInsertValidator;

    @GetMapping("/insert")
    public String getTeacherForm(Model model) {
        model.addAttribute("teacherInsertDTO",  TeacherInsertDTO.empty());
//        model.addAttribute("regionsReadOnlyDTO", regions());
        return "teacher-insert";
    }

    @PostMapping("/insert")
    public String insertTeacher(@Valid
                                    @ModelAttribute("teacherInsertDTO")
                                    TeacherInsertDTO teacherInsertDTO,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {

        teacherInsertValidator.validate(teacherInsertDTO, bindingResult);
        if (bindingResult.hasErrors()) {
//            model.addAttribute("regionsReadOnlyDTO", regions());
            return "teacher-insert";
        }
        try {
            TeacherReadOnlyDTO teacherReadOnlyDTO = teacherService.saveTeacher(teacherInsertDTO);
            //Porst redirect get
            redirectAttributes.addFlashAttribute("teacherReadOnlyDTO", teacherReadOnlyDTO);
            return "redirect:/teachers/success";    //controller
        }catch (EntityAlreadyExistsException | EntityInvalidArgumentException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "teacher-insert";
        }


    }

    @GetMapping("/success")
    public String teacherInsertSuccess(Model model) {
        if (!model.containsAttribute("teacherReadOnlyDTO")) {     // ελέγχει το F5 - refresh
            return "redirect:/teachers";
        }
        return "teacher-success";
    }


    @GetMapping({ "", "/"})
    public String getPaginatedTeachersDeletedFalse(@PageableDefault(page = 0, size = 5, sort = "lastname") Pageable pageable,
                                                   Model model) {
        Page<TeacherReadOnlyDTO> teachersPage = teacherService.getPaginatedTeachersDeletedFalse(pageable);
        model.addAttribute("teachers", teachersPage.getContent());
        model.addAttribute("page", teachersPage);
        return "teachers";
    }

    @GetMapping("/edit/{uuid}")
    public String getTeacherEdit(@PathVariable UUID uuid, Model model) throws EntityNotFoundException {
        try{
            TeacherEditDTO teacerEditDTO = teacherService.getTeacherByUUIDDeletedFalse(uuid);
            model.addAttribute("teacherEditDTO", teacerEditDTO);;

        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "teacher-edit";
    }

    @PostMapping("/edit")
    public String updateTeacher(@Valid @ModelAttribute TeacherEditDTO teacherEditDTO
            , BindingResult bindingResult
            , RedirectAttributes redirectAttributes, Model model) {

    }



    @ModelAttribute("regionsReadOnlyDTO")
    public List<RegionReadOnlyDTO> regions() {
        return regionService.findAllRegionsSortedByName();

//        return List.of(
//                new RegionReadOnlyDTO(1L, "Αθήνα"),
//                new RegionReadOnlyDTO(2L, "Θεσσαλονίκη"),
//                new RegionReadOnlyDTO(3L, "Πάτρα")
        //);
    }

}

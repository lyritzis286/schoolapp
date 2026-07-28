package gr.aueb.cf.schoolapp.controller;

import gr.aueb.cf.schoolapp.dto.RegionReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.TeacherInsertDTO;
import gr.aueb.cf.schoolapp.model.Teacher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {

//    private final ITeacherService teacherService;
//    private final IRegionService regionService;

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

    }

    @ModelAttribute("regionsReadOnlyDTO")
    public List<RegionReadOnlyDTO> regions() {

        return List.of(
                new RegionReadOnlyDTO(1L, "Αθήνα"),
                new RegionReadOnlyDTO(2L, "Θεσσαλονίκη"),
                new RegionReadOnlyDTO(3L, "Πάτρα")
        );
    }

}

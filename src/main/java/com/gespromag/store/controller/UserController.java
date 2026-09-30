package com.gespromag.store.controller;

import com.gespromag.store.dto.UserCreateDto;
import com.gespromag.store.dto.UserEditDto;
import com.gespromag.store.entity.Role;
import com.gespromag.store.entity.User;
import com.gespromag.store.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final int PAGE_SIZE = 20;

    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<User> users = userService.list(PageRequest.of(page, PAGE_SIZE, Sort.by("username").ascending()));
        model.addAttribute("users", users);
        model.addAttribute("activeMenu", "users");
        return "users/users";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("userForm", new UserCreateDto());
        model.addAttribute("roles", Role.values());
        model.addAttribute("activeMenu", "users");
        return "users/user-add";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserCreateDto form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("activeMenu", "users");
            return "users/user-add";
        }
        try {
            userService.create(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("activeMenu", "users");
            return "users/user-add";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur cree avec succes.");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.getById(id);
        UserEditDto form = new UserEditDto();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        form.setActive(user.isActive());
        model.addAttribute("userForm", form);
        model.addAttribute("userId", id);
        model.addAttribute("roles", Role.values());
        model.addAttribute("activeMenu", "users");
        return "users/user-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("userForm") UserEditDto form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("activeMenu", "users");
            return "users/user-edit";
        }
        try {
            userService.update(id, form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userId", id);
            model.addAttribute("roles", Role.values());
            model.addAttribute("activeMenu", "users");
            return "users/user-edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur mis a jour avec succes.");
        return "redirect:/users";
    }
}

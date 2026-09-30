package com.gespromag.store.controller;

import com.gespromag.store.dto.CategoryCreateDto;
import com.gespromag.store.dto.CategoryEditDto;
import com.gespromag.store.entity.Category;
import com.gespromag.store.repository.ProductRepository;
import com.gespromag.store.service.CategoryService;
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
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final int PAGE_SIZE = 20;

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Category> categories = categoryService.list(PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending()));
        model.addAttribute("categories", categories);
        model.addAttribute("activeMenu", "categories");
        return "categories/categories";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Category category = categoryService.getById(id);
        model.addAttribute("category", category);
        model.addAttribute("products", productRepository.findAllByCategoryIdAndActiveTrueOrderByNameAsc(id));
        model.addAttribute("activeMenu", "categories");
        return "categories/category-view";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryCreateDto());
        model.addAttribute("activeMenu", "categories");
        return "categories/category-add";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categoryForm") CategoryCreateDto form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "categories");
            return "categories/category-add";
        }
        try {
            categoryService.create(form);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("activeMenu", "categories");
            return "categories/category-add";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Categorie creee avec succes.");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getById(id);
        CategoryEditDto form = new CategoryEditDto();
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        form.setActive(category.isActive());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        model.addAttribute("activeMenu", "categories");
        return "categories/category-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("categoryForm") CategoryEditDto form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("activeMenu", "categories");
            return "categories/category-edit";
        }
        try {
            categoryService.update(id, form);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categoryId", id);
            model.addAttribute("activeMenu", "categories");
            return "categories/category-edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Categorie mise a jour avec succes.");
        return "redirect:/categories";
    }
}

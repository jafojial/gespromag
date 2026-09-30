package com.gespromag.store.controller;

import com.gespromag.store.dto.ProductCreateDto;
import com.gespromag.store.dto.ProductEditDto;
import com.gespromag.store.entity.Product;
import com.gespromag.store.repository.StockMovementRepository;
import com.gespromag.store.service.CategoryService;
import com.gespromag.store.service.CurrentUserProvider;
import com.gespromag.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final int PAGE_SIZE = 20;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CurrentUserProvider currentUserProvider;
    private final StockMovementRepository stockMovementRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String categoryId,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        // Les champs "Toutes les categories" / "Tous les statuts" soumettent une valeur
        // vide : la traiter comme "aucun filtre" plutot que de la transmettre telle quelle
        // (une chaine vide ne correspondrait jamais a un statut ni ne se convertirait en Long).
        Long categoryIdValue = parseCategoryId(categoryId);
        String statusValue = blankToNull(status);

        Page<Product> products = productService.search(q, categoryIdValue, statusValue,
                PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending()));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.listActive());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryIdValue);
        model.addAttribute("status", statusValue);
        model.addAttribute("activeMenu", "products");
        return "products/products";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("movements", stockMovementRepository
                .findAllByProductIdOrderByCreatedAtDesc(id, PageRequest.of(0, 10)));
        model.addAttribute("activeMenu", "products");
        return "products/product-view";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("productForm", new ProductCreateDto());
        model.addAttribute("categories", categoryService.listActive());
        model.addAttribute("activeMenu", "products");
        return "products/product-add";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("productForm") ProductCreateDto form,
                          BindingResult bindingResult,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.listActive());
            model.addAttribute("activeMenu", "products");
            return "products/product-add";
        }
        try {
            productService.create(form, currentUserProvider.get(authentication));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.listActive());
            model.addAttribute("activeMenu", "products");
            return "products/product-add";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Produit cree avec succes.");
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getById(id);
        ProductEditDto form = new ProductEditDto();
        form.setName(product.getName());
        form.setSku(product.getSku());
        form.setDescription(product.getDescription());
        form.setCategoryId(product.getCategory().getId());
        form.setPurchasePrice(product.getPurchasePrice());
        form.setSellingPrice(product.getSellingPrice());
        form.setMinimumQuantity(product.getMinimumQuantity());
        form.setActive(product.isActive());
        model.addAttribute("productForm", form);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.listActive());
        model.addAttribute("activeMenu", "products");
        return "products/product-edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("productForm") ProductEditDto form,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.listActive());
            model.addAttribute("activeMenu", "products");
            return "products/product-edit";
        }
        try {
            productService.update(id, form);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.listActive());
            model.addAttribute("activeMenu", "products");
            return "products/product-edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Produit mis a jour avec succes.");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deactivate(id);
        redirectAttributes.addFlashAttribute("successMessage", "Produit desactive avec succes.");
        return "redirect:/products";
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static Long parseCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(categoryId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

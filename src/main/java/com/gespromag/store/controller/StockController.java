package com.gespromag.store.controller;

import com.gespromag.store.dto.StockMovementFormDto;
import com.gespromag.store.entity.MovementType;
import com.gespromag.store.entity.Product;
import com.gespromag.store.entity.StockMovement;
import com.gespromag.store.repository.ProductRepository;
import com.gespromag.store.service.CurrentUserProvider;
import com.gespromag.store.service.ProductService;
import com.gespromag.store.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private static final int PAGE_SIZE = 20;

    private final StockMovementService stockMovementService;
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String alerts(Model model) {
        model.addAttribute("products", productRepository.findNeedingRestock());
        model.addAttribute("activeMenu", "stock");
        return "stock/stock-alerts";
    }

    @GetMapping("/movements")
    public String movements(@RequestParam(required = false) Long productId,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        Page<StockMovement> movements = (productId != null)
                ? stockMovementService.listByProduct(productId, PageRequest.of(page, PAGE_SIZE))
                : stockMovementService.list(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("movements", movements);
        model.addAttribute("productId", productId);
        if (productId != null) {
            model.addAttribute("product", productService.getById(productId));
        }
        model.addAttribute("activeMenu", "stock");
        return "stock/stock-movements";
    }

    @GetMapping("/movements/new")
    public String newForm(@RequestParam(required = false) Long productId, Model model) {
        StockMovementFormDto form = new StockMovementFormDto();
        form.setProductId(productId);
        model.addAttribute("movementForm", form);
        model.addAttribute("products", productRepository.findAllByActiveTrueOrderByNameAsc());
        model.addAttribute("movementTypes", MovementType.values());
        model.addAttribute("activeMenu", "stock");
        return "stock/stock-movement-add";
    }

    @PostMapping("/movements")
    public String create(@Valid @ModelAttribute("movementForm") StockMovementFormDto form,
                          BindingResult bindingResult,
                          Model model,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productRepository.findAllByActiveTrueOrderByNameAsc());
            model.addAttribute("movementTypes", MovementType.values());
            model.addAttribute("activeMenu", "stock");
            return "stock/stock-movement-add";
        }
        try {
            Product product = productService.getById(form.getProductId());
            stockMovementService.record(product, form.getType(), form.getQuantity(), form.getComment(),
                    currentUserProvider.get(authentication));
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", productRepository.findAllByActiveTrueOrderByNameAsc());
            model.addAttribute("movementTypes", MovementType.values());
            model.addAttribute("activeMenu", "stock");
            return "stock/stock-movement-add";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Mouvement de stock enregistre avec succes.");
        return "redirect:/stock/movements";
    }
}

package com.example.controller;

import com.example.model.Category;
import com.example.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Thêm danh mục");
        return "category/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable int id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        model.addAttribute("pageTitle", "Sửa danh mục");
        return "category/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Category category,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle",
                    category.getId() == 0 ? "Thêm danh mục" : "Sửa danh mục");
            return "category/form";
        }
        categoryService.saveCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}

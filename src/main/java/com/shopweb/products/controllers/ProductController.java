package com.shopweb.products.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopweb.products.models.Product;
import com.shopweb.products.models.ProductDto;
import com.shopweb.products.repositories.ProductRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductRepository repo;

	@GetMapping({ "", "/" })
	public String showProductsLists(Model model) {
		List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}

	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto", productDto);
		return "products/CreateProduct";
	}

	@PostMapping("/create")
	public String createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {

		// Sau khi submit thì code bên dưới được chạy

		if (productDto.getImageFile().isEmpty()) { // Thêm lỗi chưa up ảnh vì productDto chưa code được lỗi này
			result.addError(new FieldError("productDto", "imageFile", "The image is required"));
		}

		if (result.hasErrors()) { // Nếu có lỗi thì quay về trang CreateProduct
			return "products/CreateProduct";
		}

		MultipartFile imageFile = productDto.getImageFile();
		String imageFileName = imageFile.getOriginalFilename();
		Date createdAt = new Date();

		try {
			Path imagePath = Paths.get("public/images/" + imageFileName);
			try (InputStream inputStream = imageFile.getInputStream()) {
				Files.copy(inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			return "products/CreateProduct";
		}

		Product product = new Product();
		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());
		product.setCreatedAt(createdAt);
		product.setImageFileName(imageFileName);

		repo.save(product);
		return "redirect:/products";
	}

	@GetMapping("/edit")
	public String showEditPage(Model model, @RequestParam int id) {
		Product product = repo.findById(id).get();
		ProductDto productDto = new ProductDto();

		productDto.setName(product.getName());
		productDto.setBrand(product.getBrand());
		productDto.setCategory(product.getCategory());
		productDto.setPrice(product.getPrice());
		productDto.setDescription(product.getDescription());

		model.addAttribute("product", product);
		model.addAttribute("productDto", productDto);

		return "products/EditProduct";
	}

	@PostMapping("/edit")
	public String editProduct(Model model, @RequestParam int id, @Valid @ModelAttribute ProductDto productDto,
			BindingResult result) {

		// Sau khi submit thì code bên dưới được chạy

		Product product = repo.findById(id).get();
		model.addAttribute("product", product);

		if (result.hasErrors()) { // Nếu có lỗi thì return về trang EditProduct
			return "products/EditProduct";
		}

		// Nếu không lỗi thì cập nhật thông tin sản phẩm vào database

		product.setName(productDto.getName());
		product.setBrand(productDto.getBrand());
		product.setCategory(productDto.getCategory());
		product.setPrice(productDto.getPrice());
		product.setDescription(productDto.getDescription());

		// Nếu có yêu cầu đổi ảnh thì xoá ảnh cũ, thêm ảnh mới vào folder images
		if (!productDto.getImageFile().isEmpty()) {
			String oldImageFileName = product.getImageFileName();
			String newImageFileName = productDto.getImageFile().getOriginalFilename();
			try {
				// Xoá ảnh cũ
				Path oldImagePath = Paths.get("public/images/" + oldImageFileName);
				Files.delete(oldImagePath);

				// Thêm ảnh mới
				Path newImagePath = Paths.get("public/images/" + newImageFileName);
				try (InputStream inputStream = productDto.getImageFile().getInputStream()) {
					Files.copy(inputStream, newImagePath, StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
				return "products/EditProduct";
			}
			product.setImageFileName(newImageFileName);
		}

		repo.save(product);

		return "redirect:/products";
	}

	@GetMapping("/delete")
	public String showDeletePage(Model model, @RequestParam int id) {
		try {
			Product product = repo.findById(id).get();
			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
			Files.delete(imagePath);
			repo.delete(product);
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}

		return "redirect:/products";
	}
}

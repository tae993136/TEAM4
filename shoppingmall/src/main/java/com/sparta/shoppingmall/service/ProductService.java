package com.sparta.shoppingmall.service;

//import com.sparta.shoppingmall.dto.ProductRequestDto;
//import com.sparta.shoppingmall.dto.ProductResponseDto;
//import com.sparta.shoppingmall.entity.Product;
//import com.sparta.shoppingmall.entity.User;
//import com.sparta.shoppingmall.repository.ProductRepository;
//import com.sparta.shoppingmall.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import com.sparta.shoppingmall.dto.ProductRequestDto;
import com.sparta.shoppingmall.dto.ProductResponseDto;
import com.sparta.shoppingmall.entity.Product;
import com.sparta.shoppingmall.entity.User;
import com.sparta.shoppingmall.repository.ProductRepository;
import com.sparta.shoppingmall.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    @Transactional
    // 상품 등록하기
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).get();
        Product product = productRepository.saveAndFlush(new Product(productRequestDto, user));
        return new ProductResponseDto(product);
    }

    @Transactional
    // 상품 수정하기
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto productRequestDto) {
        // 등록된 id가 없다면 예외가 발생한다.
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("수정할 수 없습니다."));
        product.update(productRequestDto);
        // 수정 이후에 flush 하여 DB에 반영하였다.
        productRepository.flush();

        return new ProductResponseDto(productRepository.findById(product.getId()).get());
    }

    // 상품 삭제하기
    @Transactional
    public ResponseEntity<String> deleteProduct(Long productId) {
        // 등록된 id가 없다면 예외가 발생한다.
        productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("삭제할 수 없습니다."));
        productRepository.deleteById(productId);
        return new ResponseEntity("상품이 삭제 되었습니다.", HttpStatus.OK);
    }

    // 나의 판매 상품 조회
    @Transactional
    public List<ProductResponseDto> myReadProducts(int page, int size, String sortBy, boolean isAsc, UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).get();

        // 페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 현재 프론트을 사용하고 있지 않기 때문에 페이징 처리한 정보들을 리스트 형식을 반환하였다.
        Iterator<Product> products = productRepository.findByUserId(user.getId(), pageable).getContent().iterator();
        ArrayList<ProductResponseDto> productResponseDtoArrayList = new ArrayList<>();
        while (products.hasNext()) {
            productResponseDtoArrayList.add(new ProductResponseDto(products.next()));
        }

        return productResponseDtoArrayList;
    }

    // 전체 판매 상품 목록 조회
    @Transactional
    public List<ProductResponseDto> readProducts(int page, int size, String sortBy, boolean isAsc) {

        // 페이징 처리
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 현재 프론트을 사용하고 있지 않기 때문에 페이징 처리한 정보들을 리스트 형식을 반환하였다.
        Iterator<Product> products = productRepository.findAll(pageable).getContent().iterator();
        ArrayList<ProductResponseDto> productResponseDtoArrayList = new ArrayList<>();
        while (products.hasNext()) {
            productResponseDtoArrayList.add(new ProductResponseDto(products.next()));
        }

        return productResponseDtoArrayList;
    }
}

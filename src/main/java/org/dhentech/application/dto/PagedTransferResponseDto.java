package org.dhentech.application.dto;

import org.dhentech.infrastructure.entity.TransferEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class PagedTransferResponseDto {

    private List<TransferResponseDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    private PagedTransferResponseDto() {
    }

    public static PagedTransferResponseDto from(Page<TransferEntity> pageResult) {
        PagedTransferResponseDto dto = new PagedTransferResponseDto();
        dto.content = pageResult.getContent().stream()
                .map(TransferResponseDto::from)
                .collect(Collectors.toList());
        dto.page = pageResult.getNumber();
        dto.size = pageResult.getSize();
        dto.totalElements = pageResult.getTotalElements();
        dto.totalPages = pageResult.getTotalPages();
        return dto;
    }

    public List<TransferResponseDto> getContent() { return content; }

    public int getPage() { return page; }

    public int getSize() { return size; }

    public long getTotalElements() { return totalElements; }

    public int getTotalPages() { return totalPages; }
}

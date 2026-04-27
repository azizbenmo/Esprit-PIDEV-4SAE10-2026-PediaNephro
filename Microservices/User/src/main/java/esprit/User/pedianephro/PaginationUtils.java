package esprit.User.pedianephro;

import esprit.User.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    public static Pageable buildPageable(Integer page, Integer size, Sort sort) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 10 : Math.min(size, 100);
        Sort effectiveSort = (sort == null) ? Sort.by(Sort.Direction.DESC, "createdAt") : sort;
        return PageRequest.of(p, s, effectiveSort);
    }

    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}



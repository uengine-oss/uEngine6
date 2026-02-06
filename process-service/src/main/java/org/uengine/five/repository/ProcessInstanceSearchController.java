package org.uengine.five.repository;

import java.util.Date;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.uengine.five.entity.ProcessInstanceEntity;

/**
 * Oracle 프로필 전용: Spring Data REST는 커스텀 구현(Impl) 메서드를 검색 엔드포인트로 노출하지 않으므로,
 * findFilterICanSee를 /instances/search/findFilterICanSee 로 직접 노출.
 */
@Profile("oracle")
@RestController
@RequestMapping("/instances/search")
public class ProcessInstanceSearchController {

    private final ProcessInstanceRepository processInstanceRepository;

    public ProcessInstanceSearchController(ProcessInstanceRepository processInstanceRepository) {
        this.processInstanceRepository = processInstanceRepository;
    }

    @GetMapping("/findFilterICanSee")
    public Page<ProcessInstanceEntity> findFilterICanSee(
            @RequestParam(required = false) String defId,
            @RequestParam(required = false) Long instId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventHandler,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startedDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date finishedDate,
            @RequestParam(required = false) String initEp,
            @RequestParam(required = false) String prevCurrEp,
            @RequestParam(required = false) String currEp,
            @RequestParam(required = false) Boolean subProcess,
            @RequestParam(required = false) String rolePattern,
            @RequestParam(required = false) String namePattern,
            @PageableDefault(size = 20, sort = "startedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return processInstanceRepository.findFilterICanSee(
                defId, instId, status, eventHandler, name,
                startedDate, finishedDate, initEp, prevCurrEp, currEp,
                subProcess, rolePattern, namePattern, pageable);
    }
}

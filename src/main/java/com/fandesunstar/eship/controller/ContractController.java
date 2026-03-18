package com.fandesunstar.eship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fandesunstar.eship.common.result.Result;
import com.fandesunstar.eship.dto.ContractReviewDTO;
import com.fandesunstar.eship.dto.CreateContractDTO;
import com.fandesunstar.eship.service.ContractService;
import com.fandesunstar.eship.vo.ContractListVO;
import com.fandesunstar.eship.vo.ContractVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 合约控制器
 */
@Slf4j
@RestController
@RequestMapping("/contract")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    /**
     * 合约信箱（待审核）
     */
    @GetMapping("/inbox")
    public Result<IPage<ContractListVO>> getInbox(@RequestParam Long ownerId,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        IPage<ContractListVO> contracts = contractService.getOwnerInbox(ownerId, page, size);
        return Result.success(contracts);
    }

    /**
     * 同意合约
     */
    @PostMapping("/{id}/approve")
    public Result<String> approveContract(@PathVariable Long id, @RequestParam Long ownerId) {
        contractService.approveContract(id, ownerId);
        return Result.success("合约审核通过", null);
    }

    /**
     * 拒绝合约
     */
    @PostMapping("/{id}/reject")
    public Result<String> rejectContract(@PathVariable Long id,
                                         @RequestParam Long ownerId,
                                         @Valid @RequestBody ContractReviewDTO reviewDTO) {
        contractService.rejectContract(id, ownerId, reviewDTO.getRejectReason());
        return Result.success("合约已拒绝", null);
    }

    /**
     * 我的合约
     */
    @GetMapping("/my")
    public Result<IPage<ContractListVO>> getMyContracts(@RequestParam Long ownerId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size,
                                                         @RequestParam(required = false) String status) {
        IPage<ContractListVO> contracts = contractService.getMyContracts(ownerId, page, size, status);
        return Result.success(contracts);
    }

    /**
     * 合约详情
     */
    @GetMapping("/{id}")
    public Result<ContractVO> getContractDetail(@PathVariable Long id) {
        ContractVO contractVO = contractService.getContractDetail(id);
        return Result.success(contractVO);
    }

    /**
     * 创建合约
     */
    @PostMapping
    public Result<ContractVO> createContract(@RequestParam Long renterId,
                                             @Valid @RequestBody CreateContractDTO createDTO) {
        ContractVO contractVO = contractService.createContract(renterId, createDTO);
        return Result.success("合约创建成功", contractVO);
    }

    /**
     * 租家的合约
     */
    @GetMapping("/renter")
    public Result<IPage<ContractListVO>> getRenterContracts(@RequestParam Long renterId,
                                                             @RequestParam(defaultValue = "1") Integer page,
                                                             @RequestParam(defaultValue = "10") Integer size,
                                                             @RequestParam(required = false) String status) {
        IPage<ContractListVO> contracts = contractService.getRenterContracts(renterId, page, size, status);
        return Result.success(contracts);
    }

    /**
     * 取消合约
     */
    @DeleteMapping("/{id}")
    public Result<String> cancelContract(@PathVariable Long id, @RequestParam Long renterId) {
        contractService.cancelContract(id, renterId);
        return Result.success("合约取消成功", null);
    }
}

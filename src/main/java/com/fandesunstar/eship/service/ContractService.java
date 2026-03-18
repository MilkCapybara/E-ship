package com.fandesunstar.eship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fandesunstar.eship.common.exception.BusinessException;
import com.fandesunstar.eship.dto.CreateContractDTO;
import com.fandesunstar.eship.entity.Contract;
import com.fandesunstar.eship.entity.Ship;
import com.fandesunstar.eship.entity.User;
import com.fandesunstar.eship.mapper.ContractMapper;
import com.fandesunstar.eship.mapper.ShipMapper;
import com.fandesunstar.eship.mapper.UserMapper;
import com.fandesunstar.eship.service.pdf.ContractPdfService;
import com.fandesunstar.eship.vo.ContractListVO;
import com.fandesunstar.eship.vo.ContractVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 合约服务类
 */
@Slf4j
@Service
public class ContractService {

    private final ContractMapper contractMapper;
    private final ShipMapper shipMapper;
    private final UserMapper userMapper;
    private final ContractPdfService contractPdfService;
    private final MailService mailService;
    private final com.fandesunstar.eship.websocket.NotificationWebSocketHandler notificationHandler;

    public ContractService(ContractMapper contractMapper, ShipMapper shipMapper,
                          UserMapper userMapper, ContractPdfService contractPdfService,
                          MailService mailService,
                          com.fandesunstar.eship.websocket.NotificationWebSocketHandler notificationHandler) {
        this.contractMapper = contractMapper;
        this.shipMapper = shipMapper;
        this.userMapper = userMapper;
        this.contractPdfService = contractPdfService;
        this.mailService = mailService;
        this.notificationHandler = notificationHandler;
    }

    /**
     * 合约信箱（待审核）
     */
    public IPage<ContractListVO> getOwnerInbox(Long ownerId, Integer page, Integer size) {
        Page<Contract> pageParam = new Page<>(page, size);
        IPage<Contract> contractPage = contractMapper.selectOwnerInbox(pageParam, ownerId);

        return contractPage.convert(this::convertToContractListVO);
    }

    /**
     * 同意合约
     */
    @Transactional
    public void approveContract(Long contractId, Long ownerId) {
        // 验证合约存在且属于该船东
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }
        if (!contract.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权审核此合约");
        }

        // 检查合约状态
        if (!"PENDING".equals(contract.getStatus())) {
            throw new BusinessException("合约状态不是待审核");
        }

        // 更新合约状态为已同意
        contractMapper.approveContract(contractId);

        // 更新船舶状态为已租
        shipMapper.updateStatus(contract.getShipId(), "RENTED");

        log.info("合约审核通过：合约ID={}, 船东ID={}", contractId, ownerId);

        // 发送WebSocket通知给租家
        notificationHandler.sendContractNotification(contract.getRenterId(), "APPROVED", contractId);

        // 生成并发送合约PDF
        try {
            generateAndSendContractPdf(contract);
        } catch (Exception e) {
            log.error("生成或发送合约PDF失败：合约ID={}", contractId, e);
            // 不影响合约审核流程，只记录错误
        }
    }

    /**
     * 拒绝合约
     */
    @Transactional
    public void rejectContract(Long contractId, Long ownerId, String reason) {
        // 验证合约存在且属于该船东
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }
        if (!contract.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权审核此合约");
        }

        // 检查合约状态
        if (!"PENDING".equals(contract.getStatus())) {
            throw new BusinessException("合约状态不是待审核");
        }

        // 更新合约状态为已拒绝
        contractMapper.rejectContract(contractId, reason);

        log.info("合约审核拒绝：合约ID={}, 船东ID={}, 原因={}", contractId, ownerId, reason);

        // 发送WebSocket通知给租家
        notificationHandler.sendContractNotification(contract.getRenterId(), "REJECTED", contractId);
    }

    /**
     * 我的合约
     */
    public IPage<ContractListVO> getMyContracts(Long ownerId, Integer page, Integer size, String status) {
        Page<Contract> pageParam = new Page<>(page, size);
        IPage<Contract> contractPage = contractMapper.selectByOwnerId(pageParam, ownerId, status);

        return contractPage.convert(this::convertToContractListVO);
    }

    /**
     * 合约详情
     */
    public ContractVO getContractDetail(Long contractId) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }

        ContractVO contractVO = new ContractVO();
        BeanUtils.copyProperties(contract, contractVO);

        // 获取船舶信息
        Ship ship = shipMapper.selectById(contract.getShipId());
        if (ship != null) {
            contractVO.setShipName(ship.getShipName());
            contractVO.setShipType(ship.getShipType());
        }

        // 获取租家信息
        User renter = userMapper.selectById(contract.getRenterId());
        if (renter != null) {
            contractVO.setRenterName(renter.getUsername());
            contractVO.setRenterCreditScore(renter.getCreditScore());
        }

        return contractVO;
    }

    /**
     * 创建合约
     */
    @Transactional
    public ContractVO createContract(Long renterId, CreateContractDTO dto) {
        // 验证租家存在
        User renter = userMapper.selectById(renterId);
        if (renter == null) {
            throw new BusinessException("租家不存在");
        }

        // 验证船舶存在且可租
        Ship ship = shipMapper.selectById(dto.getShipId());
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }
        if (!"AVAILABLE".equals(ship.getStatus())) {
            throw new BusinessException("船舶当前不可租赁");
        }

        // 验证日期范围
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }

        // 验证租金
        if (dto.getDailyRent().compareTo(ship.getDailyRent()) < 0) {
            throw new BusinessException("日租金不能低于船舶设定的租金");
        }

        // 计算总金额
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        BigDecimal totalAmount = dto.getDailyRent().multiply(BigDecimal.valueOf(days));

        // 创建合约
        Contract contract = new Contract();
        BeanUtils.copyProperties(dto, contract);
        contract.setOwnerId(ship.getOwnerId());
        contract.setRenterId(renterId);
        contract.setTotalAmount(totalAmount);
        contract.setStatus("PENDING");

        contractMapper.insert(contract);

        log.info("合约创建成功：合约ID={}, 租家ID=, 船舶ID={}", contract.getId(), renterId, dto.getShipId());

        // 发送WebSocket通知给船东
        notificationHandler.sendContractNotification(ship.getOwnerId(), "NEW_CONTRACT", contract.getId());

        return getContractDetail(contract.getId());
    }

    /**
     * 租家的合约
     */
    public IPage<ContractListVO> getRenterContracts(Long renterId, Integer page, Integer size, String status) {
        Page<Contract> pageParam = new Page<>(page, size);
        IPage<Contract> contractPage = contractMapper.selectByRenterId(pageParam, renterId);

        // 如果指定了状态，过滤
        if (status != null && !status.isEmpty()) {
            return contractPage.convert(contract -> {
                if (contract.getStatus().equals(status)) {
                    return convertToContractListVO(contract);
                }
                return null;
            });
        }

        return contractPage.convert(this::convertToContractListVO);
    }

    /**
     * 取消合约
     */
    @Transactional
    public void cancelContract(Long contractId, Long renterId) {
        // 验证合约存在且属于该租家
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new BusinessException("合约不存在");
        }
        if (!contract.getRenterId().equals(renterId)) {
            throw new BusinessException("无权取消此合约");
        }

        // 只能取消待审核的合约
        if (!"PENDING".equals(contract.getStatus())) {
            throw new BusinessException("只能取消待审核的合约");
        }

        // 更新合约状态为已取消
        contractMapper.updateStatus(contractId, "CANCELLED");

        log.info("合约取消成功：合约ID={}, 租家ID={}", contractId, renterId);

        // 发送WebSocket通知给船东
        notificationHandler.sendContractNotification(contract.getOwnerId(), "CANCELLED", contractId);
    }

    /**
     * 转换为ContractListVO
     */
    private ContractListVO convertToContractListVO(Contract contract) {
        ContractListVO vo = new ContractListVO();
        BeanUtils.copyProperties(contract, vo);

        // 获取船舶名称
        Ship ship = shipMapper.selectById(contract.getShipId());
        if (ship != null) {
            vo.setShipName(ship.getShipName());
        }

        // 获取租家名称和信用评分
        User renter = userMapper.selectById(contract.getRenterId());
        if (renter != null) {
            vo.setRenterName(renter.getUsername());
            vo.setRenterCreditScore(renter.getCreditScore());
        }

        return vo;
    }

    /**
     * 生成并发送合约PDF
     */
    private void generateAndSendContractPdf(Contract contract) throws IOException {
        log.info("开始生成并发送合约PDF：合约ID={}", contract.getId());

        // 获取船舶信息
        Ship ship = shipMapper.selectById(contract.getShipId());
        if (ship == null) {
            throw new BusinessException("船舶不存在");
        }

        // 获取船东信息
        User owner = userMapper.selectById(contract.getOwnerId());
        if (owner == null) {
            throw new BusinessException("船东不存在");
        }

        // 获取租家信息
        User renter = userMapper.selectById(contract.getRenterId());
        if (renter == null) {
            throw new BusinessException("租家不存在");
        }

        // 生成PDF
        byte[] pdfData = contractPdfService.generateContractPdf(contract, ship, owner, renter);

        // 生成合约编号
        String contractNo = "ES-" + contract.getId() + "-" +
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 发送给船东
        try {
            mailService.sendContractPdf(owner.getEmail(), contractNo, ship.getShipName(), pdfData);
            log.info("合约PDF已发送给船东：邮箱={}", owner.getEmail());
        } catch (Exception e) {
            log.error("发送合约PDF给船东失败：邮箱={}", owner.getEmail(), e);
        }

        // 发送给租家
        try {
            mailService.sendContractPdf(renter.getEmail(), contractNo, ship.getShipName(), pdfData);
            log.info("合约PDF已发送给租家：邮箱={}", renter.getEmail());
        } catch (Exception e) {
            log.error("发送合约PDF给租家失败：邮箱={}", renter.getEmail(), e);
        }

        log.info("合约PDF生成并发送完成：合约ID={}", contract.getId());
    }
}

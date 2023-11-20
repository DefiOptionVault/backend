package com.DefiOptionVault.DOV.Order;

import com.DefiOptionVault.DOV.Option.Option;
import com.DefiOptionVault.DOV.Order.Order;
import com.DefiOptionVault.DOV.Order.OrderRepository;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    public List<Order> showOpenedPosition() {
        List<Order> orders = getAllOrders();
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            BigInteger pnl;
            try {
                pnl = new BigInteger(order.getPnl());
            } catch (NumberFormatException e) {
                pnl = BigInteger.ZERO;
            }

            if (order.getSettlementPrice().equals("0")) {
                result.add(order);
            } else if(!order.getSettled()) {
                if (order.getPosition().equals("write")) {
                    result.add(order);
                }
                if (order.getPosition().equals("purchase")
                        && pnl.compareTo(BigInteger.ZERO) > 0) {
                    result.add(order);
                }
            }
        }

        return result;
    }

    @Scheduled(cron = "0 59 23 * * SUN")
    public void setAllPnl() {
        List<Order> orders = getAllOrders();
        for (Order order : orders) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime expiry = order.getOption()
                    .getExpiry()
                    .toInstant()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();
            if (expiry.isBefore(now) && order.getPnl().equals("0")) {
                String symbol = order.getSymbol();
                if (symbol.substring(symbol.length() - 3).equals("PUT")) {

                    //order.setSettlementPrice();

                    BigInteger settlement = new BigInteger(order.getSettlementPrice());
                    BigInteger strike = new BigInteger(order.getStrikePrice());
                    BigInteger pnl = settlement.subtract(strike);

                    if (pnl.signum() == -1) {
                        order.setPnl(String.valueOf(0));
                    } else {
                        order.setPnl(String.valueOf(pnl));
                    }
                }
                if (symbol.substring(symbol.length() - 4).equals("CALL")) {
                    BigInteger settlement = new BigInteger(order.getSettlementPrice());
                    BigInteger strike = new BigInteger(order.getStrikePrice());
                    order.setPnl(String.valueOf(strike.subtract(settlement)));
                }
            }
        }
    }
}
//openedOrder.get().
//settlementPrice : 만기 시 가격으로 업뎃
//Pnl : strikePrice 에서 빼서 넣기
//strikePrice - settelmentPrice(put옵션 이익)
//큰 경우는 Pnl 0
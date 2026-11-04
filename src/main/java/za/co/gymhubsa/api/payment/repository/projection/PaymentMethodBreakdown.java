package za.co.gymhubsa.api.payment.repository.projection;

import za.co.gymhubsa.api.payment.entity.PaymentMethod;

import java.math.BigDecimal;

/** Spring Data JPA interface projection for grouping completed payments by method. */
public interface PaymentMethodBreakdown {

    PaymentMethod getMethod();

    BigDecimal getTotal();

    Long getCount();
}

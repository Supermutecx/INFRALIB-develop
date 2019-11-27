package ro.infrasoft.infralib.bean.help;

/**
 * Reprezinta un item de 'order by'.
 */
public class Order {
    private String order;
    private String orderDir;

    /**
     * Constructor default.
     */
    public Order() {
    }

    /**
     * Constructor care primeste order si order dir.
     *
     * @param order    coloana dupa care sa ordoneze
     * @param orderDir directia in care sa ordoneze
     */
    public Order(String order, String orderDir) {
        this.order = order;
        this.orderDir = orderDir;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderDir() {
        return orderDir;
    }

    public void setOrderDir(String orderDir) {
        this.orderDir = orderDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Order order1 = (Order) o;

        return order.equals(order1.order);

    }

    @Override
    public int hashCode() {
        return order.hashCode();
    }
}

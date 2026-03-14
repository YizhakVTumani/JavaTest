package org.example;

public class Main {
    public static void main(String[] args) {

    }
}
class Money{
    int cents;
    int dollars;
}

class Order {

}

class OrderItem {

}

class Email {
    String email;
}

interface PaymentMethod {
    public void payment();
}

class PayByCard implements PaymentMethod {
    @Override
    public void payment(){

    }
}

class PayByPayPal implements PaymentMethod {
    @Override
    public void payment(){

    }
}

abstract class OrderProcessorTemplate {
    private final void process(){
        //Validation
        //Розрахунок
        //Оплата
        //Завершення/повідомлення
    }
    private void validate(){

    }
    private void calculate(){

    }
    private void payment(){

    }
    private void notifyUser(){

    }
}


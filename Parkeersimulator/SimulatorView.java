package Parkeersimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SimulatorView extends JFrame {
    private CarParkView carParkView;
    private AdvanceButton advanceButton;
    private int numberOfFloors;
    private int numberOfRows;
    private int numberOfPlaces;
    private int numberOfOpenSpots;
    private Car[][][] cars;
    private int passPlaces;
    private Location lastPass;
    private int amount;

    public SimulatorView(int numberOfFloors, int numberOfRows, int numberOfPlaces, int passPlaces) {
        this.numberOfFloors = numberOfFloors;
        this.numberOfRows = numberOfRows;
        this.numberOfPlaces = numberOfPlaces;
        this.numberOfOpenSpots =numberOfFloors*numberOfRows*numberOfPlaces;
        this.passPlaces = passPlaces;
        //passPlaces = passPlaces > numberOfOpenSpots ? numberOfOpenSpots : passPlaces ;  TODO Test dit en kijken of het nodig is
        passPlaces = passPlaces < 0 ? 0 : passPlaces;
        amount = passPlaces;
        cars = new Car[numberOfFloors][numberOfRows][numberOfPlaces];

        carParkView = new CarParkView();
        advanceButton = new AdvanceButton();


        Container contentPane = getContentPane();
        contentPane.add(carParkView, BorderLayout.CENTER);
        contentPane.add(advanceButton, BorderLayout.NORTH);

        pack();
        setVisible(true);

        updateView();
    }

    public void updateView() {
        carParkView.updateView();
    }
    public int getPassPlaces(){
      return passPlaces;
    }

	public int getNumberOfFloors() {
        return numberOfFloors;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public int getNumberOfPlaces() {
        return numberOfPlaces;
    }

    public int getNumberOfOpenSpots(){
    	return numberOfOpenSpots;
    }

    public Car getCarAt(Location location) {
        if (!locationIsValid(location)) {
            return null;
        }
        return cars[location.getFloor()][location.getRow()][location.getPlace()];
    }

    public boolean setCarAt(Location location, Car car) {
        if (!locationIsValid(location)) {
            return false;
        }
        Car oldCar = getCarAt(location);
        if (oldCar == null) {
            cars[location.getFloor()][location.getRow()][location.getPlace()] = car;
            car.setLocation(location);
            numberOfOpenSpots--;
            return true;
        }
        return false;
    }

    public Car removeCarAt(Location location) {
        if (!locationIsValid(location)) {
            return null;
        }
        Car car = getCarAt(location);
        if (car == null) {
            return null;
        }
        cars[location.getFloor()][location.getRow()][location.getPlace()] = null;
        car.setLocation(null);
        numberOfOpenSpots++;
        return car;
    }

    public Location getFirstFreeLocation(boolean paying) {
        for (int floor = 0; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                  if (paying == true){
                    if (floor <= lastPass.getFloor()){
                      floor = lastPass.getFloor();
                      if (row <= lastPass.getRow()){
                        row = lastPass.getRow();
                        place = place <= lastPass.getPlace() ? lastPass.getPlace() + 1 : place ;
                      }
                    }
                  }
                  Location location = new Location(floor, row, place);
                  Location check = getCarAt(location) == null ? location : null;
                  if (check != null){
                    return location;
                  }
                }
              }
            }
            return null;
          }

    public Car getFirstLeavingCar() {
        for (int floor = 0; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    Car car = getCarAt(location);
                    if (car != null && car.getMinutesLeft() <= 0 && !car.getIsPaying()) {
                        return car;
                    }
                }
            }
        }
        return null;
    }

    public void tick() {
        for (int floor = 0; floor < getNumberOfFloors(); floor++) {
            for (int row = 0; row < getNumberOfRows(); row++) {
                for (int place = 0; place < getNumberOfPlaces(); place++) {
                    Location location = new Location(floor, row, place);
                    Car car = getCarAt(location);
                    if (car != null) {
                        car.tick();
                    }
                }
            }
        }
    }

    private boolean locationIsValid(Location location) {
        int floor = location.getFloor();
        int row = location.getRow();
        int place = location.getPlace();
        if (floor < 0 || floor >= numberOfFloors || row < 0 || row > numberOfRows || place < 0 || place > numberOfPlaces) {
            return false;
        }
        return true;
    }

    private class CarParkView extends JPanel {

        private Dimension size;
        private Image carParkImage;

        /**
         * Constructor for objects of class CarPark
         */
        public CarParkView() {
            size = new Dimension(0, 0);
        }

        /**
         * Overridden. Tell the GUI manager how big we would like to be.
         */
        public Dimension getPreferredSize() {
            return new Dimension(800, 500);
        }

        /**
         * Overriden. The car park view component needs to be redisplayed. Copy the
         * internal image to screen.
         */
        public void paintComponent(Graphics g) {
            if (carParkImage == null) {
                return;
            }

            Dimension currentSize = getSize();
            if (size.equals(currentSize)) {
                g.drawImage(carParkImage, 0, 0, null);
            }
            else {
                // Rescale the previous image.
                g.drawImage(carParkImage, 0, 0, currentSize.width, currentSize.height, null);
            }
        }

        public void updateView() {
            // Create a new car park image if the size has changed.
            if (!size.equals(getSize())) {
                size = getSize();
                carParkImage = createImage(size.width, size.height);
            }
            Graphics graphics = carParkImage.getGraphics();
            int passLeft = getPassPlaces();
            for(int floor = 0; floor < getNumberOfFloors(); floor++) {
                for(int row = 0; row < getNumberOfRows(); row++) {
                    for(int place = 0; place < getNumberOfPlaces(); place++) {
                      Location location = new Location(floor, row, place);
                      Car car = getCarAt(location);
                      Color color = Color.white;
                      if (passLeft > 0){
                        color = car == null ? Color.yellow : car.getColor();
                        if (amount > 0){
                          lastPass = location;
                          amount--;
                        }
                        passLeft--;
                      }
                      else {
                        color = car == null ? color : car.getColor();
                      }
                      drawPlace(graphics, location, color);
                    }
                }
            }
            repaint();
        }

        /**
         * Paint a place on this car park view in a given color.
         */
        private void drawPlace(Graphics graphics, Location location, Color color) {
            graphics.setColor(color);
            graphics.fillRect(
                    location.getFloor() * 260 + (1 + (int)Math.floor(location.getRow() * 0.5)) * 75 + (location.getRow() % 2) * 20,
                    60 + location.getPlace() * 10,
                    20 - 1,
                    10 - 1); // TODO use dynamic size or constants
        }
    }

    private class AdvanceButton extends JPanel
    							implements ActionListener
    {

    	public JButton ButtonPlusOne, ButtonPlusHunderd;

    	public AdvanceButton()
    	{
    		ButtonPlusOne = new JButton("+1");
    		ButtonPlusOne.setToolTipText("Simuleer 1 minuut");

    		ButtonPlusHunderd = new JButton("+100");
    		ButtonPlusHunderd.setToolTipText("Simuleer 100 minuten");

    		ButtonPlusOne.addActionListener(this);
    		ButtonPlusHunderd.addActionListener(this);

    		ButtonPlusOne.setActionCommand("plus1");
    		ButtonPlusHunderd.setActionCommand("Plus100");

    		add(ButtonPlusOne);
    		add(ButtonPlusHunderd);


    	}

    	public void actionPerformed(ActionEvent e)
    	{
    		 if ("plus1".equals(e.getActionCommand()))
    		 {
    			 Simulator.simulateByMinute(1);
    		 }
    		 else if ("Plus100".equals(e.getActionCommand()))
    		 {
    			 Simulator.simulateByMinute(100);
    		 }
    	}

    }

}

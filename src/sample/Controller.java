
package sample;
import javafx.application.Platform;

import javafx.event.Event;
import javafx.fxml.FXML;

import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

import java.io.File;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Controller {
    @FXML Label d1;
    @FXML Label d2;
    @FXML Label d3;
    @FXML Label d4;
    @FXML Label d5;
    @FXML Label d6;
    @FXML Label d7;
    @FXML Label d8;

    @FXML ImageView radio;

    @FXML ImageView knob;

    @FXML Button onoff;
    @FXML Button mod;
    @FXML Button scn;
    @FXML Button ams;
    @FXML Button mu;
    @FXML Button btn1;
    @FXML Button btn2;
    @FXML Button btn3;
    @FXML Button btn4;
    @FXML Button btn5;
    @FXML Button btn6;
    @FXML Button btnprev;
    @FXML Button btnnext;
    @FXML Button btnbnd;
    @FXML Button btndisp;

    ArrayList<Label> chars = new ArrayList<>();
    ArrayList<Button> buttons = new ArrayList<>();

    Map<Double, String> allChanels = new TreeMap<>();

    double[][] chanels = { {90.1, 92.6, 93.5, 100.4, 103.5, 93.8}, {89.4, 97.1, 102.4, 100.4, 96.9, 93.8}, {101.6, 92.6, 93.5, 87.9, 97.9, 105} };

    int bnd; //FM1/FM2/FM3

    Timer timer;

    boolean muted;
    boolean radioIsOn;

    boolean modRadio;

    double volume; //od 0 do 1
    Double activeStation;

    String bip = "beep.mp3";
    Media hit = new Media(new File(bip).toURI().toString());

    String noise = "static noise.mp3";
    Media radioNoise = new Media(new File(noise).toURI().toString());

    MediaPlayer mediaPlayer = new MediaPlayer(hit);

    double startX;
    double startRotate;
    double prevPos, nextPos;

    private long lastUpdate;

    @PostConstruct
    public void initialize() {
        initializeScreen();

        buttons.add(mod);
        buttons.add(scn);
        buttons.add(ams);
        buttons.add(mu);
        buttons.add(btn1);
        buttons.add(btn2);
        buttons.add(btn3);
        buttons.add(btn4);
        buttons.add(btn5);
        buttons.add(btn6);
        buttons.add(btnprev);
        buttons.add(btnnext);
        buttons.add(btnbnd);
        buttons.add(btndisp);

        initializeChannels();

        bnd = 1;

        modRadio = true;

        activeStation = 90.1;


        for(Label l:chars) unsetDigit(l);
        setScreen(0, "FM190.1");
        turnOnRadio();

        timer = new Timer();
        timer.cancel();

        muted = false;
        volume = 0.25;

        radioIsOn = true;

        playMusic();
    }

    private void initializeScreen(){
        chars.add(d1);
        chars.add(d2);
        chars.add(d3);
        chars.add(d4);
        chars.add(d5);
        chars.add(d6);
        chars.add(d7);
        chars.add(d8);
    }

    private void initializeChannels(){
        allChanels.put(90.1, "music.mp3");
        allChanels.put(92.6, "music2.mp3");
        allChanels.put(93.5, "music3.mp3");
        allChanels.put(100.4, "music4.mp3");
        allChanels.put(103.5, "music5.mp3");
        allChanels.put(93.8, "music6.mp3");
        allChanels.put(89.4, "music7.mp3");
        allChanels.put(97.1, "music.mp3");
        allChanels.put(102.4, "music2.mp3");
        allChanels.put(96.9, "music3.mp3");
        allChanels.put(101.6, "music4.mp3");
        allChanels.put(87.9, "music5.mp3");
        allChanels.put(97.9, "music6.mp3");
        allChanels.put(105.0, "music7.mp3");


        double[][] chanels = { {90.1, 92.6, 93.5, 100.4, 103.5, 93.8}, {89.4, 97.1, 102.4, 100.4, 96.9, 93.8}, {101.6, 92.6, 93.5, 87.9, 97.9, 105.0} };
    }

    boolean isChannel(Double d){
        return allChanels.containsKey(d);
    }

    private void animation(String text, String action){
        if(!action.contains("noanimation")) for(Label l:chars) unsetDigit(l);
        timer.cancel();
        timer = new Timer();

        boolean x = true;

        timer.scheduleAtFixedRate(new TimerTask() {
            int interval = 8;
            int wait = 4;
            public void run() {
                if(interval > 0)
                {
                    if(action.contains("noanimation"))
                    {
                        Platform.runLater(() -> setScreen(0, text));
                        interval = 0;
                    }else{
                        Platform.runLater(() -> setScreen(interval, text));
                        interval--;
                    }
                }
                else {
                    if(wait > 0 && !action.equals("none"))
                    {
                        wait--;
                    }
                    else{
                        Platform.runLater(() -> actionAfterAnimation(action));
                        timer.cancel();
                    }
                }
            }
        }, 125,125);
    }

    private void actionAfterAnimation(String action){
        switch (action){
            case "off":
                turnOffRadio();
                break;
            case "noanimation nostopmusic":
                if(radioIsOn && modRadio) {
                    setScreen(0, getTextToShowOnTheScree());
                }
                break;
            default:
                if(radioIsOn && modRadio) {
                    setScreen(0, getTextToShowOnTheScree());
                    playMusic();
                }
                break;
        }
    }

    public void mousePressKnob(MouseEvent event){
        Point2D pt = knob.localToParent(event.getX(), event.getY());
        startX = pt.getX();
        startRotate = knob.getRotate();
        prevPos = (3 * (startX) + startRotate)%360;;
        nextPos = (3 * (startX) + startRotate)%360;;
    }

    public void btnPressed(){
        Date date = new Date();

        lastUpdate = date.getTime();
    }

    public void btnReleased(Event event){
        if(!radioIsOn || !modRadio) return;
        int idButton = Integer.parseInt(((Button) event.getSource()).getId().substring(3));

        Date date = new Date();
        long tmp = date.getTime()-lastUpdate;

        if(tmp>500){
            recBtn(idButton);
            playSound();
        }else{
            btnClick(idButton);
        }
    }

    public void btnClick(int id){
        if(!radioIsOn || !modRadio) return;
        playSound();

        activeStation = chanels[bnd-1][id-1];
        animation(getTextToShowOnTheScree(), "noanimation");
    }

    public void recBtn(int id){
        if(!radioIsOn || !modRadio) return;

        chanels[bnd-1][id-1] = activeStation;
        animation(getTextToShowOnTheScree(), "noanimation");
    }

    public void mouseDraggedKnob(MouseEvent event){
        if(!radioIsOn) return;
        prevPos = nextPos;
        Point2D pt = knob.localToParent(event.getX(), event.getY());
        double x = pt.getX();
        double newRotate = (3 * (x - startX) + startRotate)%360;
        nextPos = newRotate;
        if(nextPos>prevPos){
            if(volume<1)
                volume += 0.01;
        }else if(nextPos<prevPos){
            if(volume>0)
                volume-=0.01;
        }
        knob.setRotate(newRotate);
        if(radioIsOn) setScreen(0, "Vol "+(int)(volume*100));

        if(!muted) mediaPlayer.setVolume(volume);
    }

    public void mouseReleaseKnob(){
        if(radioIsOn) animation("Vol "+(int)(volume*100), "noanimation nostopmusic");
    }

    private void playMusic(){
        if(!isChannel(activeStation)){
            playNoise();
            return;
        }



        String music = allChanels.get(activeStation);
        Media radioPlay = new Media(new File(music).toURI().toString());

        mediaPlayer.stop();
        mediaPlayer = new MediaPlayer(radioPlay);
        mediaPlayer.setVolume((muted?0:volume));

        if(muted) mediaPlayer.setVolume(0);
        mediaPlayer.play();
    }

    private void playNoise(){
        mediaPlayer.stop();
        mediaPlayer = new MediaPlayer(radioNoise);
        mediaPlayer.setVolume((muted?0:volume));

        if(muted) mediaPlayer.setVolume(0);
        mediaPlayer.play();
    }

    private void setScreen(int from, String text){
        while (text.length()<8) text+=" ";
        for(int i=0; i<8; i++){
            if(from-i<=0){
                if(!text.substring(i-from,  i-from + 1).equals(" ")) {
                    setDigit(text.substring(i-from,  i-from + 1), chars.get(i));
                }
                else
                    unsetDigit(chars.get(i));
            }
            else{
                unsetDigit(chars.get(i));
            }
        }

    }

    public void scnClick(){
        if(!radioIsOn || !modRadio) return;

        playSound();

        for(int i=1; i<=6; i++){
            activeStation = searchNextStation(activeStation+0.1);
            recBtn(i);
        }

        animation(getTextToShowOnTheScree(), "noanimation");
    }

    public void amsClick(){
        if(!radioIsOn || !modRadio) return;

        playSound();

        activeStation = searchNextStation(activeStation+0.1);
        animation(getTextToShowOnTheScree(), "noanimation");
    }

    private double searchNextStation(Double d){
        while (!allChanels.containsKey(d)){
            d+=0.1;

            d*=10;
            d = (double) Math.round(d);
            d/=10;

            if(d>110) d = 80.0;
        }
        return d;

    }

    public void prevClick(){
        if(!radioIsOn || !modRadio) return;

        activeStation = activeStation-0.1;
        activeStation*=10;
        activeStation = (double) Math.round(activeStation);
        activeStation/=10;
        animation(getTextToShowOnTheScree(), "noanimation");
    }

    public void nextClick(){
        if(!radioIsOn || !modRadio) return;

        activeStation = activeStation+0.1;
        activeStation*=10;
        activeStation = (double) Math.round(activeStation);
        activeStation/=10;
        animation(getTextToShowOnTheScree(), "noanimation");
    }

    public void bndClick(){
        if(!radioIsOn || !modRadio) return;

        bnd = ((bnd)%3);
        bnd+=1;
        playSound();
        animation("FM"+bnd, "noanimation");
    }

    public void dispClick(){
        if(!radioIsOn || !modRadio) return;

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        if(!d5.getText().equals(":")) animation("  "+simpleDateFormat.format(date), "noanimation nostopmusic");
        else animation(getTextToShowOnTheScree(), "noanimation nostopmusic");
    }

    public void muClick(){
        if(!radioIsOn) return;
        muted =! muted;
        if(muted) {
            setScreen(0, "MUTE");
            mediaPlayer.setVolume(0);
        }
        else {
            animation("unMute", "noanimation nostopmusic");
            mediaPlayer.setVolume(volume);
        }
    }

    public void onoffClick(){
        if(radioIsOn){
            playSound();
            animation("Good-bye", "off");
        }else{
            turnOnRadio();
            animation("Welcome", "play");
        }
        radioIsOn = !radioIsOn;
    }

    private void turnOnRadio(){
        switchOn(radio);

        onoff.getStyleClass().remove(0, onoff.getStyleClass().size());
        onoff.getStyleClass().add("btnon");


        for(Button b:buttons) b.setVisible(true);

    }

    private void turnOffRadio(){
        switchOff(radio);

        mediaPlayer.stop();

        for(Button b:buttons) b.setVisible(false);
        for(Label l:chars) unsetDigit(l);

        onoff.getStyleClass().remove(0, onoff.getStyleClass().size());
        onoff.getStyleClass().add("btnoff");

    }

    public void modClick(){
        if(!radioIsOn) return;
        playSound();
        if(modRadio){
            animation("Aux in", "wait");
        }else{
            animation("Radio", "wait");
        }
        modRadio = !modRadio;
    }

    private void playSound(){
        mediaPlayer.stop();
        if(!muted){
            mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.setVolume(volume);
            mediaPlayer.play();
        }
    }

    private String getTextToShowOnTheScree(){
        if(muted) return "MUTE";
        return "FM"+bnd+activeStation;
    }

    private void setDigit(String d, Label l){
        switchOn(l);
        l.setText(d);
    }

    private void unsetDigit(Label l){
        switchOff(l);
        l.setText("8");
    }

    private void switchOn(Label l){
        if(!isActive(l)) {
            l.getStyleClass().remove(0, l.getStyleClass().size());
            l.getStyleClass().add("label-active");
        }
    }

    private void switchOff(Label l){
        if(isActive(l)) {
            l.getStyleClass().remove(0, l.getStyleClass().size());
            l.getStyleClass().add("label-inactive");
        }
    }

    private void switchOn(ImageView i){
        if(!isActive(i)) {
            i.getStyleClass().remove(0, i.getStyleClass().size());
            i.getStyleClass().add("radioOn");
        }
    }

    private void switchOff(ImageView i){
        if(isActive(i)) {
            i.getStyleClass().remove(0, i.getStyleClass().size());
            i.getStyleClass().add("radioOff");
        }
    }

    private boolean isActive(Label l){
        return l.getStyleClass().get(0).equals("label-active");
    }

    private boolean isActive(ImageView i){
        return i.getStyleClass().get(0).equals("radioOn");
    }

}

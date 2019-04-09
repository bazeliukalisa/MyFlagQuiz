package lt.vtmc.ems.zwaclaw.myflagquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainActivityFragment extends Fragment {

    /*Tag for Logging*/
    private static final String TAG = "My Flag Quiz Activity";
    /*Flag number used in quiz*/
    private static final int FLAGS_IN_QUIZ = 10;
    /*File names with flag images*/
    private List<String> fileNameList;
    /*File list with flag images for quiz*/
    private List<String> quizCountriesList;
    /*Selected regions for quiz*/
    private Set<String> regionSet;
    /*Good coutry name for flag*/
    private String correctAnswer;
    /*All answers number*/
    private int totalGuesses;
    /*Good answers number*/
    private int correctAnswers;
    /*Number rows for buttons*/
    private int guessRows;
    /*Object for randomise ...*/
    private SecureRandom random;
    /*Object for new flag latency ...*/
    private Handler handler;
    /*Wrong answer animation ...*/
    private Animation shakeAnimation;
    /*Main application layout*/
    private LinearLayout quizLinearLayout;
    /*View for quiz number question ...*/
    private TextView questionNumberTextView;
    /*View for flag image ...*/
    private ImageView flagImageView;
    /*List for buttons with questions ...*/
    private LinearLayout[] guessLinearLayouts;
    /*View for correct answer in the quiz ...*/
    private TextView answerTextView;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*Graphical user interface initialization for fragment*/
        super.onCreateView(inflater, container, savedInstanceState);
        /*Get fragment layout*/
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        /*Selected fields initialization*/
        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();
        /*Animation initialization*/
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        /*Animation repeat count*/
        shakeAnimation.setRepeatCount(3);
        /*User interface components initializations*/
        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);
        /*Configurations for buttons listener*/
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++){
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
        /*Show formatted text in TextView*/
        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));
        /*Return fragment View*/
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences){
        /*Get button number for view*/
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        /*Rows number for buttons*/
        guessRows = Integer.parseInt(choices) / 2;
        for (LinearLayout layout : guessLinearLayouts) {
            layout.setVisibility(View.GONE);
        }
        /*Show rows with buttons*/
        for (int row = 0; row < guessRows; row++) {
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    public void updateRegions(SharedPreferences sharedPreferences){
        /*Get selected regions by user*/
        regionSet = sharedPreferences.getStringSet(MainActivity.REGIONS, null);
    }

    public void resetQuiz(){
        /*Get acces to asset folder*/
        AssetManager assets = getActivity().getAssets();
        /*Clear list with flags*/
        fileNameList.clear();
        /*Get flags files name from selected regions*/
        try {
            /*Iteration in assets folder*/
            for (String region : regionSet) {
                /*Get all file names from selected region*/
                String[] paths = assets.list(region);
                /*Delete images png extension from file name*/
                for (String path : paths) {
                    fileNameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error when load flags images", ex);
        }
        /*Reset correct and all answers number*/
        correctAnswers = 0;
        totalGuesses = 0;
        /*Clear countries list*/
        quizCountriesList.clear();
        /*Variables initialization for flags randomisation*/
        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        /*Flags generation*/
        while (flagCounter <= FLAGS_IN_QUIZ) {
            /*Index selector from 0 to number of flags in quiz*/
            int randomIndex = random.nextInt(numberOfFlags);
            /*Get file name for selected Index*/
            String fileName = fileNameList.get(randomIndex);
            /*If file name not selected, put to countries list*/
            if (!quizCountriesList.contains(fileName)){
                quizCountriesList.add(fileName);
                ++flagCounter;
            }
        }
        /*Load next flag*/
        loadNextFlag();
    }

    private void loadNextFlag() {
        /*Determine the file name of the current flag*/
        String nextImage = quizCountriesList.remove(0);
        /*Update correct answer*/
        correctAnswer = nextImage;
        /*Clear the TextView view*/
        answerTextView.setText("");
        /*Display the current question number*/
        questionNumberTextView.setText(getString(R.string.question,(correctAnswers + 1), FLAGS_IN_QUIZ));
        /*Retrieve the name of the current flag area*/
        String region = nextImage.substring(0, nextImage.indexOf("-"));
        /*Access to asset folder*/
        AssetManager assets = getActivity().getAssets();
        /*Open, load and fill the image of the flag in the ImageView view*/
        try (InputStream inputStream = assets.open(region + "/" + nextImage + ".png")){
            /*Loading the flag image as a Drawable object*/
            Drawable drawableFlag = Drawable.createFromStream(inputStream, nextImage);
            /*Assign a Drawable object (flags) in the ImageView view*/
            flagImageView.setImageDrawable(drawableFlag);
            /*Animation of the flag entry on the screen*/
            animate(false);
        } catch (IOException ex) {
            Log.e(TAG, "Error loading ..." + nextImage, ex);
        }
        /*Mixing of file names*/
        Collections.shuffle(fileNameList);
        /*Putting the correct answer at the end of the list*/
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));
        /*Addition of text to response buttons*/
        for (int row = 0; row < guessRows; row++){
            for (int column = 0; column < 2; column++){
                /*Accessing the button and changing its status to "enabled"*/
                Button guessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                guessButton.setEnabled(true);
                /*Get the country name and set it in the Button view*/
                String fileName = fileNameList.get((row * 2) + column);
                guessButton.setText(getCountryName(fileName));
            }
        }
        /*Adding the correct answer to the randomly selected button*/
        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayouts[row];
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    private String getCountryName(String name){
        return name.substring(name.indexOf("-") + 1).replace("_", " ");
    }

    private void animate(boolean animateOut) {
        /*We do not create animations when displaying the first flag*/
        if (correctAnswers == 0) return;
        /*Calculation of coordinates of the distribution center*/
        int centerX = (quizLinearLayout.getLeft() + quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() + quizLinearLayout.getBottom()) / 2;
        /*Calculation of the animation radius*/
        int radius = Math.max(quizLinearLayout.getWidth(), quizLinearLayout.getHeight());
        /*Defining an animation object*/
        Animator animator;
        /*Variant of the animation covering the flag*/
        if (animateOut) {
            /*Create animation*/
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, radius, 0);
            /*When animation is end ...*/
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        }
        /*Variant of the animation revealing the wave*/
        else {
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX, centerY, 0, radius);
        }
        /*Determine the duration of the animation*/
        animator.setDuration(500);
        /*Launching the animation*/
        animator.start();
    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*Downloading the button pressed and the text displayed by it*/
            Button guessButton = (Button) v;
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            /*Increment of the number of responses given by the user in the quiz*/
            ++totalGuesses;
            /*If the answer is correct*/
            if (guess.equals(answer)) {
                /*Incrementing the number of correct answers*/
                ++correctAnswers;
                /*Display feedback for the user about the correct answer*/
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));
                /*Deactivation of all answer buttons*/
                disableButtons();
                /*If the user has answered all the questions*/
                if (correctAnswers == FLAGS_IN_QUIZ) {
                    /*Creation of the AlertDialog object with personalized text and a button*/
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Quiz results");
                    builder.setMessage(getString(R.string.results, totalGuesses, (1000 / (double) totalGuesses)));
                    builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetQuiz();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }
                /*If the user did not answer all the questions*/
                else {
                    /*Wait 2 seconds and load another flag*/
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animate(true);
                        }
                    }, 2000);
                }
            }
            /*If the answer is not correct*/
            else {
                /*Reproduction of the shaking flag animation*/
                flagImageView.startAnimation(shakeAnimation);
                /*Display feedback for the user about the wrong answer*/
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                /*Deactivation of a button with an incorrect answer*/
                guessButton.setEnabled(false);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int column = 0; column < 2; column++) {
                guessRow.getChildAt(column).setEnabled(false);
            }
        }
    }
}

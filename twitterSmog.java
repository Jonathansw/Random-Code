import java.io.*;
import java.net.*;
import com.google.gson.*;
import com.temboo.Library.Twitter.OAuth.FinalizeOAuth;
import com.temboo.Library.Twitter.OAuth.InitializeOAuth;
import com.temboo.Library.Twitter.Timelines.UserTimeline;
import com.temboo.core.TembooSession;

/**
 * Created by John, jsw76 on 2/15/16.
 * Midterm Practicum
 * For all the keys you just need to put in your own
 * This uses temboo to make the twitter calls
 */


public class twitterSmog {
    public static double calcGrade(double totalPoly, double totalSentences) {
        double grade = 0;
        grade = 1.0430 * Math.sqrt(totalPoly * (30 / totalSentences)) + 3.1291;
        return grade;
    }

    public static void main(String arg[]) throws Exception {
        String clientID; //Temboo clientID
        String clientSecret; // Temboo clientSecret
        String acctName; //Temboo account name
        String appName; //Temboo application name
        String appKey;  //Twitter app key
        String user = "@BernieSanders"; //Twitter user, to check SMOG grade
        String wordNikFront = "http://api.wordnik.com:80/v4/word.json/";
        String wordNikEnd = //wordnik ending url containing user api key

        String oAuthSecret;
        String callBackID;
        String accessToken;
        String accessTokenSecret;
        String jsonOutput;
        String tweet;
        String wordNik;

        String[] sentence;

        int sentenceCount = 0;

        double polyGrade = 0;
        int sentencePoly = 0;
        int temp = 0;

        TembooSession session = new TembooSession(acctName, appName, appKey);

        //First oAuth step
        InitializeOAuth initializeOAuthChoreo = new InitializeOAuth(session);
        InitializeOAuth.InitializeOAuthInputSet initializeOAuthInputs = initializeOAuthChoreo.newInputSet();

        // Set inputs for initializeOAuth
        initializeOAuthInputs.set_ConsumerKey(clientID);
        initializeOAuthInputs.set_ConsumerSecret(clientSecret);

        InitializeOAuth.InitializeOAuthResultSet initializeOAuthResults = initializeOAuthChoreo.execute(initializeOAuthInputs);

        oAuthSecret = initializeOAuthResults.get_OAuthTokenSecret();
        callBackID = initializeOAuthResults.get_CallbackID();

        //Make user to go authorize before continuing
        System.out.println("Go here: " + initializeOAuthResults.get_AuthorizationURL());
        System.out.println("Press enter when done with authorization.");
        System.in.read();

        //Second step to oAuth
        FinalizeOAuth finalizeOAuthChoreo = new FinalizeOAuth(session);
        FinalizeOAuth.FinalizeOAuthInputSet finalizeOAuthInputs = finalizeOAuthChoreo.newInputSet();

        // Set inputs
        finalizeOAuthInputs.set_ConsumerKey(clientID);
        finalizeOAuthInputs.set_ConsumerSecret(clientSecret);
        finalizeOAuthInputs.set_CallbackID(callBackID);
        finalizeOAuthInputs.set_OAuthTokenSecret(oAuthSecret);

        FinalizeOAuth.FinalizeOAuthResultSet finalizeOAuthResults = finalizeOAuthChoreo.execute(finalizeOAuthInputs);

        accessToken = finalizeOAuthResults.get_AccessToken();
        accessTokenSecret = finalizeOAuthResults.get_AccessTokenSecret();


        //Grabs the timeline/tweets of the user specified
        UserTimeline userTimelineChoreo = new UserTimeline(session);

        // Get an InputSet object for the choreo
        UserTimeline.UserTimelineInputSet userTimelineInputs = userTimelineChoreo.newInputSet();

        // Set inputs
        userTimelineInputs.set_ConsumerSecret(clientSecret);
        userTimelineInputs.set_ConsumerKey(clientID);
        userTimelineInputs.set_AccessTokenSecret(accessTokenSecret);
        userTimelineInputs.set_AccessToken(accessToken);
        userTimelineInputs.set_ScreenName(user);
        userTimelineInputs.set_IncludeRetweets(false);
        userTimelineInputs.set_ExcludeReplies(true);
        userTimelineInputs.set_Count(100);

        // Execute Choreo
        UserTimeline.UserTimelineResultSet userTimelineResults = userTimelineChoreo.execute(userTimelineInputs);

        jsonOutput = userTimelineResults.get_Response();
        JsonParser outputJp = new JsonParser();
        JsonElement textJp = outputJp.parse(jsonOutput);
        JsonArray textArray = textJp.getAsJsonArray();
        //Parses for only tweets, removing punctuation, urls as well as all retweets
        for (int i = 0; i < 30; i++) {
            JsonObject textObj = textArray.get(i).getAsJsonObject();
            tweet = textObj.get("text").getAsString();
            tweet = tweet.replaceAll("https?://\\S+\\s?", "");
            tweet = tweet.replaceAll("@?", "");
            tweet = tweet.replaceAll("#?", "");
            tweet = tweet.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ");
            tweet = tweet.toLowerCase();
            sentenceCount++; //counts the number of sentences, should be 40

            //loops through the sentence word by word
            sentence = tweet.split(" ");
            for (int x = 0; x < sentence.length; x++) {
                if (sentence[x].isEmpty()) {
                    continue;
                }
                wordNik = wordNikFront + sentence[x] + wordNikEnd;

                URL wordNikURL = new URL(wordNik);

                URLConnection wordConnection = wordNikURL.openConnection();
                InputStream wordResponse = wordConnection.getInputStream();
                InputStreamReader wordIsr = new InputStreamReader(wordResponse);

                JsonParser wordPaser = new JsonParser();
                JsonElement wordRoot = wordPaser.parse(wordIsr);
                JsonArray wordArray = wordRoot.getAsJsonArray();

                temp = (wordArray.size()) + 1;
                if (temp >= 3) {
                    sentencePoly += temp;
                }
            }
        }
        polyGrade = calcGrade(sentencePoly, sentenceCount);
        System.out.println("1.0430 * sqrt( " + sentencePoly + " * ( 30 / " + sentenceCount + " )) + 3.1291 = " + polyGrade);
        System.out.println("The Grade of " + user + " is: " + Math.round(polyGrade));
    }
}

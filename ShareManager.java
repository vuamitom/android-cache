package com.knx.nation.common;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.knx.nation.R;
import com.knx.nation.component.WebActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Tam
 * Date: 10/1/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShareManager implements WebDialog.OnCompleteListener{
    public static final String TITLE = "title";
    public static final String TEXT_CONTENT = "content";
    public static final String DESCRIPTION = "description";
    public static final String HTML_CONTENT = "html";
    public static final String PICTURE = "picture";
    public static final String URL = "url";

    Context context;
    FacebookManager facebookManager;

    public ShareManager(Context ctx){
        context = ctx;
    }

    public FacebookManager getFacebookManager(){
        if(facebookManager == null){
            facebookManager = new FacebookManager(context);
        }
        return facebookManager;
    }

    public static final String TAG = "ShareManager";


    //========== facebook share ==================//



    public void shareViaFacebook(Map<String, String> details){

        try{
            //try to look for native app activity
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, details.get(TITLE));
            intent.setType("text/plain");



            final PackageManager pm = context.getPackageManager();
            final List activityList = pm.queryIntentActivities(intent, 0);
            int len =  activityList.size();
            int i;
            for (i = 0; i < len; i++) {
                final ResolveInfo app = (ResolveInfo) activityList.get(i);
                Log.i(TAG, "activityinfo " + app.activityInfo.name);
                if ("com.facebook.katana.activity.composer.ImplicitShareIntentHandler".equals(app.activityInfo.name)) {

                    final ActivityInfo activity=app.activityInfo;
                    final ComponentName name=new ComponentName(activity.applicationInfo.packageName, activity.name);
                    Log.i(TAG, "activity start ====> activity " + activity.name);
                    //final ComponentName name=new ComponentName("com.facebook.katana", "com.facebook.katana.activity.composer.ComposerActivity");
                    intent=new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    //intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setComponent(name);
                    intent.putExtra(Intent.EXTRA_TEXT, details.get(TITLE) + " " + details.get(URL) + " via " + context.getResources().getString(R.string.app_name)) ;
                    context.startActivity(intent);
                    break;
                }

            }
            if(i == len)
                throw new ActivityNotFoundException();
        }
        catch(final ActivityNotFoundException e) {
            //revert to using FB SDK
            Session session =Session.getActiveSession();
            Bundle params = new Bundle();
            params.putString("name", details.get(TITLE));
            params.putString("caption", "via " + context.getResources().getString(R.string.app_name));
            if(details.containsKey(DESCRIPTION))
                params.putString("description", details.get(DESCRIPTION));
            params.putString("link", details.get(URL));
            if(details.containsKey(PICTURE))
                params.putString("picture", details.get(PICTURE));

//        if(session == null || !session.isOpened()){
//            Log.i(TAG, "login to facebook");
//            getFacebookManager().login();
//        }
//        else{
//            postToFB(details, session);
//        }

            getFacebookManager().publish(params);
        }


    }

//    private void postToFB(Map<String , String> details, Session session){
//
//        Log.i(TAG, "already logged in to facebook");
//        WebDialog feedDialog = (
//                new WebDialog.FeedDialogBuilder(context,
//                        session,
//                        params))
//                .setOnCompleteListener(this)
//                .build();
//        feedDialog.show();
//    }

//    public Session.StatusCallback callback = new Session.StatusCallback(){
//        @Override
//        public void call(Session session, SessionState state, Exception exception) {
//            onSessionStateChange(session, state, exception);
//        }
//    };
//
//    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
//        Log.d(TAG, "FB session change: session is opened " + state.isOpened());
//
//        if(state.equals((SessionState.OPENED))){
//            if(needToShare && shareDetails!=null){
//                postToFB(shareDetails, session);
//                needToShare = false;
//                shareDetails = null;
//            }
//        }
//        else if(state.isClosed()){
//
//        }
//    }
    //========= end facebook share ===============//
    @Override
    public void onComplete(Bundle values,
                           FacebookException error) {
        if (error == null) {
            // When the story is posted, echo the success
            // and the post Id.
            final String postId = values.getString("post_id");
            if (postId != null) {
                Toast.makeText(context,
                        "Posted story, id: "+postId,
                        Toast.LENGTH_SHORT).show();
            } else {
                // User clicked the Cancel button
                Toast.makeText(context.getApplicationContext(),
                        "Publish cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (error instanceof FacebookOperationCanceledException) {
            // User clicked the "x" button
            Toast.makeText(context.getApplicationContext(),
                    "Publish cancelled",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Generic, ex: network error
            Toast.makeText(context.getApplicationContext(),
                    "Error posting story",
                    Toast.LENGTH_SHORT).show();
        }
    }



    public void shareViaTwitter(Map<String, String> details){
        try{

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, details.get(TITLE));
            intent.setType("text/plain");
            final PackageManager pm = context.getPackageManager();
            final List activityList = pm.queryIntentActivities(intent, 0);
            int len =  activityList.size();
            int i;
            for (i = 0; i < len; i++) {
                final ResolveInfo app = (ResolveInfo) activityList.get(i);
                if ("com.twitter.android.PostActivity".equals(app.activityInfo.name)) {

                    final ActivityInfo activity=app.activityInfo;
                    final ComponentName name=new ComponentName(activity.applicationInfo.packageName, activity.name);
                    intent=new Intent(Intent.ACTION_SEND);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setComponent(name);
                    intent.putExtra(Intent.EXTRA_TEXT, details.get(TITLE) + " " + details.get(URL) + " via " + context.getResources().getString(R.string.app_name)) ;
                    context.startActivity(intent);
                    break;
                }

            }
            if(i == len)
                throw new ActivityNotFoundException();
        }
        catch(final ActivityNotFoundException e) {
            Log.i(TAG, "no twitter native", e);
            //share using web intent instead
            Intent tweet = new Intent(this.context, WebActivity.class);
            String url = null;
            try {
                url = "https://twitter.com/intent/tweet?url=" + URLEncoder.encode(details.get(URL), "utf-8");
                url += "&via=NationNews%20Mobile";
                url += "&text=" + URLEncoder.encode(details.get(TITLE), "utf-8");
                Log.d(TAG, "url = " + url );
                tweet.putExtra("url", url);
                tweet.putExtra("title", "Tweet");
                this.context.startActivity(tweet);
            } catch (UnsupportedEncodingException e1) {
                Log.e(TAG, " ", e1);

            }

        }
    }

    public void shareViaEmail(Map<String, String> details){
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","", null));
        //i.setType("message/rfc822");

        //i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
        if(details.containsKey(TITLE))
            i.putExtra(Intent.EXTRA_SUBJECT, "[ " + context.getResources().getString(R.string.app_name) + "]" + details.get(TITLE));

        if(details.containsKey(TEXT_CONTENT)){
            i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(details.get(TEXT_CONTENT)));
            //text content must be supplied together with html content
//            if(details.containsKey(HTML_CONTENT))
//                i.putExtra(Intent.EXTRA_HTML_TEXT   , details.get(HTML_CONTENT));
        }
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}

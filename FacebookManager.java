package com.knx.nation.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.facebook.*;
import com.facebook.internal.SessionAuthorizationType;
import com.facebook.internal.SessionTracker;
import com.facebook.internal.Utility;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Tam
 * Date: 10/1/13
 * Time: 10:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class FacebookManager implements WebDialog.OnCompleteListener {
    public static final String TAG = "FacebookManager";


    //private static final String TAG = LoginButton.class.getName();
    private String applicationId = null;
    private SessionTracker sessionTracker;
    private GraphUser user = null;
    private Session userInfoSession = null; // the Session used to fetch the current user info
    private boolean confirmLogout;
    private boolean fetchUserInfo;
    private UserInfoChangedCallback userInfoChangedCallback;
    private Fragment parentFragment;
    private LoginButtonProperties properties = new LoginButtonProperties();
    private Context context;
    private Bundle toPublishPost;

    public Context getContext(){
        return  context;

    }

    public void startTracking(){
        if(sessionTracker!= null && !sessionTracker.isTracking()){
            sessionTracker.startTracking();
            fetchUserInfo();
        }
    }

    public void stopTracking(){
        if(sessionTracker!=null)
            sessionTracker.stopTracking();
    }

    @Override
    public void onComplete(Bundle values, FacebookException error) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    static class LoginButtonProperties {
        private SessionDefaultAudience defaultAudience = SessionDefaultAudience.FRIENDS;
        private List<String> permissions = Collections.<String>emptyList();
        private SessionAuthorizationType authorizationType = null;
        private OnErrorListener onErrorListener;
        private SessionLoginBehavior loginBehavior = SessionLoginBehavior.SSO_WITH_FALLBACK;
        private Session.StatusCallback sessionStatusCallback;


        public void setOnErrorListener(OnErrorListener onErrorListener) {
            this.onErrorListener = onErrorListener;
        }

        public OnErrorListener getOnErrorListener() {
            return onErrorListener;
        }

        public void setDefaultAudience(SessionDefaultAudience defaultAudience) {
            this.defaultAudience = defaultAudience;
        }

        public SessionDefaultAudience getDefaultAudience() {
            return defaultAudience;
        }

        public void setReadPermissions(List<String> permissions, Session session) {
            if (SessionAuthorizationType.PUBLISH.equals(authorizationType)) {
                throw new UnsupportedOperationException(
                        "Cannot call setReadPermissions after setPublishPermissions has been called.");
            }
            if (validatePermissions(permissions, SessionAuthorizationType.READ, session)) {
                this.permissions = permissions;
                authorizationType = SessionAuthorizationType.READ;
            }
        }

        public void setPublishPermissions(List<String> permissions, Session session) {
            if (SessionAuthorizationType.READ.equals(authorizationType)) {
                throw new UnsupportedOperationException(
                        "Cannot call setPublishPermissions after setReadPermissions has been called.");
            }
            if (validatePermissions(permissions, SessionAuthorizationType.PUBLISH, session)) {
                this.permissions = permissions;
                authorizationType = SessionAuthorizationType.PUBLISH;
            }
        }

        private boolean validatePermissions(List<String> permissions,
                                            SessionAuthorizationType authType, Session currentSession) {
            if (SessionAuthorizationType.PUBLISH.equals(authType)) {
                if (Utility.isNullOrEmpty(permissions)) {
                    throw new IllegalArgumentException("Permissions for publish actions cannot be null or empty.");
                }
            }
            if (currentSession != null && currentSession.isOpened()) {
                if (!Utility.isSubset(permissions, currentSession.getPermissions())) {
                    Log.e(TAG, "Cannot set additional permissions when session is already open.");
                    return false;
                }
            }
            return true;
        }


    }

    /**
     * Specifies a callback interface that will be called when the button's notion of the current
     * user changes (if the fetch_user_info attribute is true for this control).
     */
    public interface UserInfoChangedCallback {
        /**
         * Called when the current user changes.
         * @param user  the current user, or null if there is no user
         */
        void onUserInfoFetched(GraphUser user);
    }

    /**
     * Callback interface that will be called when a network or other error is encountered
     * while logging in.
     */
    public interface OnErrorListener {
        /**
         * Called when a network or other error is encountered.
         * @param error     a FacebookException representing the error that was encountered.
         */
        void onError(FacebookException error);
    }



    public FacebookManager(Context context) {
        this.context = context;
        initializeActiveSessionWithCachedToken(context);
        sessionTracker = new SessionTracker(getContext(), new LoginButtonCallback(), null, false);
        sessionTracker.startTracking();
        fetchUserInfo = false;
        //fetchUserInfo();
    }




    /**
     * Provides an implementation for {@link android.app.Activity#onActivityResult
     * onActivityResult} that updates the Session based on information returned
     * during the authorization flow. The Activity containing this view
     * should forward the resulting onActivityResult call here to
     * update the Session state based on the contents of the resultCode and
     * data.
     *
     * @param requestCode
     *            The requestCode parameter from the forwarded call. When this
     *            onActivityResult occurs as part of Facebook authorization
     *            flow, this value is the activityCode passed to open or
     *            authorize.
     * @param resultCode
     *            An int containing the resultCode parameter from the forwarded
     *            call.
     * @param data
     *            The Intent passed as the data parameter from the forwarded
     *            call.
     * @return A boolean indicating whether the requestCode matched a pending
     *         authorization request for this Session.
     * @see Session#onActivityResult(android.app.Activity, int, int, android.content.Intent)
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Session session = sessionTracker.getSession();
        if (session != null) {
            return session.onActivityResult((Activity)getContext(), requestCode,
                    resultCode, data);
        } else {
            return false;
        }
    }


    public Session.StatusCallback getSessionStatusCallback(){
        return properties.sessionStatusCallback;
    }

    public void setSessionStatusCallback(Session.StatusCallback callback){
        properties.sessionStatusCallback = callback;
    }

    /**
     * Set the Session object to use instead of the active Session. Since a Session
     * cannot be reused, if the user logs out from this Session, and tries to
     * log in again, a new Active Session will be used instead.
     * <p/>
     * If the passed in session is currently opened, this method will also attempt to
     * load some user information for display (if needed).
     *
     * @param newSession the Session object to use
     * @throws FacebookException if errors occur during the loading of user information
     */
    public void setSession(Session newSession) {
        sessionTracker.setSession(newSession);
        fetchUserInfo();
    }



//    // For testing purposes only
//    List<String> getPermissions() {
//        return properties.getPermissions();
//    }
//
//    void setProperties(LoginButtonProperties properties) {
//        this.properties = properties;
//    }




    private boolean initializeActiveSessionWithCachedToken(Context context) {
        if (context == null) {
            return false;
        }

        Session session = Session.getActiveSession();
        if (session != null) {
            return session.isOpened();
        }

        applicationId = Utility.getMetadataApplicationId(context.getApplicationContext());
        Log.d(TAG, "applicationId = " + applicationId);
        if (applicationId == null) {
            return false;
        }

        return Session.openActiveSessionFromCache(context) != null;
    }

    private void fetchUserInfo() {
        if (fetchUserInfo) {
            final Session currentSession = sessionTracker.getOpenSession();
            if (currentSession != null) {
                if (currentSession != userInfoSession) {
                    Request request = Request.newMeRequest(currentSession, new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser me,  Response response) {
                            if (currentSession == sessionTracker.getOpenSession()) {
                                user = me;
                                if (userInfoChangedCallback != null) {
                                    userInfoChangedCallback.onUserInfoFetched(user);
                                }
                            }
                            if (response.getError() != null) {
                                handleError(response.getError().getException());
                            }
                        }
                    });
                    Request.executeBatchAsync(request);
                    userInfoSession = currentSession;
                }
            } else {
                user = null;
                if (userInfoChangedCallback != null) {
                    userInfoChangedCallback.onUserInfoFetched(user);
                }
            }
        }
    }


    public void login(){
        final Session openSession = sessionTracker.getOpenSession();
        if(openSession == null){
            Session currentSession = sessionTracker.getSession();
            Log.i(TAG, "get current session");

            if (currentSession == null || currentSession.getState().isClosed()) {
                Log.i(TAG, "current session is closed");
                sessionTracker.setSession(null);
                Session session = new Session.Builder(context).setApplicationId(applicationId).build();
                Session.setActiveSession(session);
                currentSession = session;
            }


            currentSession.addCallback(new LoginButtonCallback());

            if (!currentSession.isOpened()) {
                Log.i(TAG, "session not opened " + context);
                Session.OpenRequest openRequest = null;

                if (parentFragment != null) {
                    openRequest = new Session.OpenRequest(parentFragment);
                } else if (context instanceof Activity) {
                    Log.i(TAG, "opening session request");
                    openRequest = new Session.OpenRequest((Activity)context);
                } else if ( context instanceof FragmentActivity){
                    Log.i(TAG, "opening session request");
                    openRequest = new Session.OpenRequest((FragmentActivity)context);
                }

                if (openRequest != null) {
                    openRequest.setDefaultAudience(properties.defaultAudience);
                    openRequest.setPermissions(properties.permissions);
                    openRequest.setLoginBehavior(properties.loginBehavior);
                    //currentSession.openForPublish(openRequest);
                    if (SessionAuthorizationType.PUBLISH.equals(properties.authorizationType)) {
                        currentSession.openForPublish(openRequest);
                    } else {
                        currentSession.openForRead(openRequest);
                    }
                }
            }
        }
    }


    public void publish(Bundle params){

        final Session  session = sessionTracker.getOpenSession();
        if(session!=null){
            Log.i(TAG, "publishing");
            WebDialog feedDialog = (
                    new WebDialog.FeedDialogBuilder(context,
                            session,
                            params))
                    .setOnCompleteListener(this)
                    .build();
            feedDialog.show();
        }
        else{
            toPublishPost = params;
            login();
        }
    }


    private class LoginButtonCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state,
                         Exception exception) {
            fetchUserInfo();
            Log.i(TAG, "loginbutton call back ");

            if (exception != null) {
                Log.e(TAG, "", exception);
                handleError(exception);
            }
//            else if(toPublishPost!=null){
//                Log.i(TAG, "will publishi");
//                publish(toPublishPost);
//                toPublishPost = null;
//            }

            if(session.isOpened() && toPublishPost != null){
                publish(toPublishPost);
                toPublishPost = null;
            }

            if (properties.sessionStatusCallback != null) {
                properties.sessionStatusCallback.call(session, state, exception);
            }
        }
    };

    void handleError(Exception exception) {
        if (properties.onErrorListener != null) {
            if (exception instanceof FacebookException) {
                properties.onErrorListener.onError((FacebookException)exception);
            } else {
                properties.onErrorListener.onError(new FacebookException(exception));
            }
        }
    }
}

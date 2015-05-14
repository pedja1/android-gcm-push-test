package com.tehnicomsolutions.pushtest.app.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by pedja on 11/18/13 10.14.
 * This class is part of the ${PROJECT_NAME}
 * Copyright © 2014 ${OWNER}
 * @author Predrag Čokulov, Google(copy/paste from gcm setup guide :D)
 */
public class GcmIntentService extends IntentService
{
    private static final int NOTIFICATION_ID_MESSAGE = 1010;
    public static final String PUSH_NOTIFICATION_DATA = "push_notification_data";


    public GcmIntentService()
    {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        final String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())// has effect of unparcelling Bundle
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                Handler handler = new Handler(Looper.getMainLooper());
                final String message = extras.getString("data");
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LocalBroadcastManager.getInstance(GcmIntentService.this).sendBroadcast(new Intent("push").putExtra("message", message));
                    }
                });

                /*PushNotification pushNotification = parseGcmMessage(extras);

                // Post notification of received message.
                if(pushNotification != null)
                {
                    if(MainApp.getInstance().getCurrentActivity() != null)
                    {
                        if(pushNotification.isMessageType())
                        {
                            //if foreground activity is ConversationActivity and thread ids match, just send message to it directly
                            if(MainApp.getInstance().getCurrentActivity() instanceof ConversationActivity
                                    && isSameThread((ConversationActivity) MainApp.getInstance().getCurrentActivity(), pushNotification)
                                    && ((ConversationActivity) MainApp.getInstance().getCurrentActivity()).isVisible())
                            {
                                sendMessageToActivity(pushNotification);
                            }
                            else
                            {
                                sendNotification(pushNotification);
                            }
                        }
                        //TODO other types, if any
                        else
                        {
                            sendNotification(pushNotification);
                        }
                    }
                    else
                    {
                        sendNotification(pushNotification);
                    }
                }*/
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /*private PushNotification parseGcmMessage(Bundle bundle)
    {
        PushNotification notification = new PushNotification();
        try
        {
            notification.setType(PushNotification.Type.valueOf(bundle.getString(JSONUtility.KEY.event_type.toString())));
            notification.setTitle(bundle.getString(JSONUtility.KEY.title.toString()));
            String sender = bundle.getString(JSONUtility.KEY.sender.toString());
            if(sender != null)
            {
                JSONObject jSender = new JSONObject(sender);
                if(jSender.has(JSONUtility.KEY.user_id.toString()))notification.setUser_id(jSender.getInt(JSONUtility.KEY.user_id.toString()));
                if(jSender.has(JSONUtility.KEY.username.toString()))notification.setUsername(jSender.getString(JSONUtility.KEY.username.toString()));
                if(jSender.has(JSONUtility.KEY.avatar.toString()))notification.setAvatar_url(jSender.getString(JSONUtility.KEY.avatar.toString()));
            }
            String data = bundle.getString(JSONUtility.KEY.data.toString());
            if(data != null && data.startsWith("{"))//means its JSONObject
            {
                JSONObject jData = new JSONObject(data);
                if(jData.has(JSONUtility.KEY.thread_id.toString()))notification.setThreadId(Utility.parseInt(jData.getString(JSONUtility.KEY.thread_id.toString()), -1));
                if(jData.has(JSONUtility.KEY.flirt_id.toString()))notification.setThreadId(Utility.parseInt(jData.getString(JSONUtility.KEY.flirt_id.toString()), -1));//TODO maybe thread id and flirt id arent the same
                if(jData.has(JSONUtility.KEY.body.toString()))notification.setText(jData.getString(JSONUtility.KEY.body.toString()));
                if(jData.has(JSONUtility.KEY.comment.toString()))notification.setComment(jData.getString(JSONUtility.KEY.comment.toString()));
                if(jData.has(JSONUtility.KEY.message.toString()))notification.setMessage(jData.getString(JSONUtility.KEY.message.toString()));
                if(jData.has(JSONUtility.KEY.url.toString()))notification.setUrl(jData.getString(JSONUtility.KEY.url.toString()));
                if(jData.has(JSONUtility.KEY.subject.toString()))notification.setSubject(jData.getString(JSONUtility.KEY.subject.toString()));
                if(jData.has(JSONUtility.KEY.ts.toString()))notification.setTs(jData.getLong(JSONUtility.KEY.ts.toString()));
                if (jData.has(JSONUtility.KEY.attachment.toString()) && jData.get(JSONUtility.KEY.attachment.toString()) instanceof JSONArray)
                {
                    JSONArray attachments = jData.getJSONArray(JSONUtility.KEY.attachment.toString());
                    List<Photo> photos = new ArrayList<>();
                    int size = attachments.length();
                    for (int a = 0; a < size; a++)
                    {
                        Photo photo = new Photo();
                        JSONObject attachment = attachments.getJSONObject(a);
                        if (attachment.has(JSONUtility.KEY.image_id.toString()))
                            photo.setImage_id(attachment.getInt(JSONUtility.KEY.image_id.toString()));
                        if (attachment.has(JSONUtility.KEY.gallery_id.toString()))
                            photo.setGallery_id(attachment.getInt(JSONUtility.KEY.gallery_id.toString()));
                        if (attachment.has(JSONUtility.KEY.version.toString()))
                            photo.setVersion(attachment.getInt(JSONUtility.KEY.version.toString()));
                        if (attachment.has(JSONUtility.KEY.src.toString()))
                            photo.setPhoto(attachment.getString(JSONUtility.KEY.src.toString()));
                        photos.add(photo);
                    }
                    notification.setPhotos(photos);
                }
            }
            return notification;
        }
        catch (Exception e)
        {
            if(BuildConfig.DEBUG) e.printStackTrace();
            if(BuildConfig.DEBUG) Log.e(Constants.LOG_TAG, "JSONUtility " + e.getMessage());
            Crashlytics.logException(e);
            return null;
        }
    }*/

    /*private void sendMessageToActivity(PushNotification pushNotification)
    {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ConversationActivity.INTENT_ACTION_MESSAGE_RECEIVED)
                .putExtra(ConversationActivity.INTENT_EXTRA_MESSAGE, pushNotification));
    }

    private boolean isSameThread(ConversationActivity activity, PushNotification pushNotification)
    {
        return activity.getThreadId() == pushNotification.getThreadId();
    }

    // Put the message into a notification and post it.
    private void sendNotification(PushNotification pushNotification)
    {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Class<? extends Activity> cls = getActivity(pushNotification);
        Intent dataIntent = new Intent(this, cls);
        if(cls == MyAccountActivity.class)
        {
            dataIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        setDataToIntent(pushNotification, dataIntent);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(cls);
        stackBuilder.addNextIntent(dataIntent);

        PendingIntent contentIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        //users avatar
        Bitmap userAvatar = ImageLoader.getInstance().loadImageSync(pushNotification.getAvatar_url());
        mBuilder.setLargeIcon(userAvatar == null ? BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher) : userAvatar);

        //maybe type icon(message icon for example if its message)
        mBuilder.setSmallIcon(getSmallIcon(pushNotification));

        mBuilder.setContentTitle(getContentTitle(pushNotification));

        mBuilder.setStyle(getNotificationStyle(pushNotification));

        mBuilder.setContentText(Html.fromHtml(getContentText(pushNotification)));
        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.FLAG_ONLY_ALERT_ONCE);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(getNotificationId(pushNotification.getType()), mBuilder.build());
    }

    private int getNotificationId(PushNotification.Type type)
    {
        switch (type)
        {
            case you_have_new_message:
            case you_have_new_reply_message:
                return NOTIFICATION_ID_MESSAGE;
            case you_have_new_flirt:
                return NOTIFICATION_ID_FLIRT;
            case somebody_viewed_your_profile:
                return NOTIFICATION_ID_PROFILE_VIEW;
            case somebody_liked_your_profile:
                return NOTIFICATION_ID_PROFILE_LIKE;
            case somebody_upladed_photo:
                return NOTIFICATION_ID_PHOTO_UPLOAD;
            case new_matches_nearby:
                return NOTIFICATION_ID_NEW_MATCHES;
            case you_have_new_chat_request:
                return NOTIFICATION_ID_CHAT_REQUEST;
            case campaign:
                return NOTIFICATION_ID_CAMPAIGN;
        }
        return -1;
    }

    private String getContentText(PushNotification notification)
    {
        switch (notification.getType())
        {
            case you_have_new_message:
            case you_have_new_reply_message:
            case you_have_new_flirt:
            case you_have_new_chat_request:
                return notification.getText();
            case somebody_viewed_your_profile:
                return getString(R.string.update_7, notification.getUsername());
            case somebody_liked_your_profile:
                return notification.getComment();
            case somebody_upladed_photo:
                return getString(R.string.update_4, notification.getUsername());
            case new_matches_nearby:
                return "";
            case campaign:
                return notification.getMessage();
        }
        return null;
    }

    private NotificationCompat.Style getNotificationStyle(PushNotification notification)
    {
        return new NotificationCompat.BigTextStyle().bigText(notification.getText() == null ? "" : Html.fromHtml(notification.getText()));
    }*/

    /**Get notification title*/
    /*private String getContentTitle(PushNotification notification)
    {
        switch (notification.getType())
        {
            case campaign:
                return notification.getSubject();
            case you_have_new_message:
            case you_have_new_reply_message:
            case you_have_new_flirt:
            case you_have_new_chat_request:
            case somebody_viewed_your_profile:
            case somebody_liked_your_profile:
            case somebody_upladed_photo:
            case new_matches_nearby:
            default:
                return  notification.getTitle();
        }
    }

    Class<? extends Activity> getActivity(PushNotification notification)
    {
        switch (notification.getType())
        {
            case you_have_new_message:
            case you_have_new_reply_message:
            case you_have_new_flirt:
            case you_have_new_chat_request:
                return ConversationActivity.class;
            case somebody_viewed_your_profile:
            case somebody_liked_your_profile:
                return ProfileActivity.class;
            case somebody_upladed_photo:
                return PhotosActivity.class;
            case new_matches_nearby:
                return BrowseSearchActivity.class;
            case campaign:
            default:
                return MyAccountActivity.class;
        }
    }

    int getSmallIcon(PushNotification notification)
    {
        switch (notification.getType())
        {
            case you_have_new_message:
            case you_have_new_reply_message:
            case you_have_new_flirt:
                return R.drawable.ic_stat_message;
            case you_have_new_chat_request:
                return R.drawable.ic_stat_chat;
            case somebody_viewed_your_profile:
                return R.drawable.ic_stat_profile_view;
            case somebody_liked_your_profile:
                return R.drawable.ic_stat_like;
            case somebody_upladed_photo:
                return R.drawable.ic_stat_photo;
            case new_matches_nearby:
                return R.drawable.ic_stat_nearby;
            case campaign:
            default:
                return R.drawable.ic_launcher;
        }
    }

    void setDataToIntent(PushNotification notification, Intent intent)
    {
        switch (notification.getType())
        {
            case you_have_new_message:
            case you_have_new_reply_message:
            case you_have_new_flirt:
            case you_have_new_chat_request:
                intent.putExtra(ConversationActivity.TID_KEY, notification.getThreadId());
                intent.putExtra(ConversationActivity.USERNAME_KEY, notification.getUsername());
                break;
            case somebody_viewed_your_profile:
            case somebody_liked_your_profile:
                intent.putExtra(ProfileActivity.KEY_USERNAME, notification.getUsername());
                break;
            case somebody_upladed_photo:
                intent.putExtra(PVActivityBase.USERNAME, notification.getUsername());
                break;
            case new_matches_nearby:
                intent.putExtra(BrowseSearchActivity.REQUEST_CODE, NetAction.REQUEST_CODE_NEARBY);
                break;
            case campaign:
        }
    }*/

}

package in.ureport.views.holders;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import br.com.ilhasoft.support.tool.DateFormatter;
import br.com.ilhasoft.support.tool.ResourceUtil;
import de.hdodenhof.circleimageview.CircleImageView;
import in.ureport.R;
import in.ureport.helpers.ImageLoader;
import in.ureport.helpers.ValueEventListenerAdapter;
import in.ureport.listener.OnNeedUpdateStoryListener;
import in.ureport.listener.OnUserStartChattingListener;
import in.ureport.managers.UserViewManager;
import in.ureport.models.Media;
import in.ureport.models.Story;
import in.ureport.models.User;
import in.ureport.models.holders.StoryHolder;
import in.ureport.network.StoryServices;
import in.ureport.views.adapters.StoriesAdapter;

/**
 * Created by johncordeiro on 17/09/15.
 */
public class StoryItemViewHolder extends RecyclerView.ViewHolder {

    private final StoriesAdapter.OnStoryViewListener onStoryViewListener;
    private final OnUserStartChattingListener onUserStartChattingListener;
    private final OnNeedUpdateStoryListener onNeedUpdateStoryListener;
    private OnStoryLikesBindingListener onStoryLikesBindingListener;

    private final CircleImageView authorPicture;
    private final TextView authorName;
    private final TextView publishedDate;
    private final RoundedImageView coverImage;
    private final TextView title;
    private final TextView markers;
    private final TextView content;
    private final TextView contributionsCount;
    private final TextView likeCount;
    private final TextView readFullStory;

    protected Story story;
    private StoryServices storyServices;
    private int primaryColorRes;

    private UserViewManager userViewManager;

    public StoryItemViewHolder(View itemView, StoriesAdapter.OnStoryViewListener onStoryViewListener,
                               OnUserStartChattingListener onUserStartChattingListener,
                               OnNeedUpdateStoryListener onNeedUpdateStoryListener) {
        super(itemView);
        this.onStoryViewListener = onStoryViewListener;
        this.onUserStartChattingListener = onUserStartChattingListener;
        this.onNeedUpdateStoryListener = onNeedUpdateStoryListener;
        primaryColorRes = new ResourceUtil(itemView.getContext()).getColorByAttr(R.attr.colorPrimary);

        authorPicture = itemView.findViewById(R.id.authorPicture);
        authorName = itemView.findViewById(R.id.authorName);
        publishedDate = itemView.findViewById(R.id.publishedDate);
        coverImage = itemView.findViewById(R.id.cover);
        title = itemView.findViewById(R.id.title);
        markers = itemView.findViewById(R.id.markers);
        content = itemView.findViewById(R.id.content);
        contributionsCount = itemView.findViewById(R.id.contributionsCount);
        likeCount = itemView.findViewById(R.id.likeCount);

        readFullStory = itemView.findViewById(R.id.readFullStory);
        readFullStory.setOnClickListener(onReadFullStoryClickListener);
        readFullStory.setTextColor(primaryColorRes);

        userViewManager = new UserViewManager(itemView.getContext());
        storyServices = new StoryServices();
    }

    public void setOnStoryLikesBindingListener(final OnStoryLikesBindingListener listener) {
        this.onStoryLikesBindingListener = listener;
    }

    public void bindInfo(@StringRes int info) {
        readFullStory.setText(info);
    }

    public void bind(Story story) {
        this.story = story;
        bindPublishDate(story);

        if (story.getUserObject() != null) {
            bindAuthor(story.getUserObject());
            bindLikes(story.getLikes());
            bindContributions(story.getContributions());
        } else if (onNeedUpdateStoryListener != null) {
            StoryHolder storyHolder = onNeedUpdateStoryListener.loadStoryData(story);
            bindStoryHolder(storyHolder);
        }
        bindCover(story.getCover());
        bindMarkers(story);

        content.setText(story.getContent());
        title.setText(story.getTitle());

        authorPicture.setOnClickListener(onUserClickListener);
        authorName.setOnClickListener(onUserClickListener);
    }

    private void bindPublishDate(Story story) {
        DateFormatter dateFormatter;
        dateFormatter = new DateFormatter();

        String timeElapsed = dateFormatter.getTimeElapsed(story.getCreatedDate()
                , itemView.getContext().getString(R.string.date_now));
        this.publishedDate.setText(timeElapsed.toLowerCase());
    }

    private void bindStoryHolder(StoryHolder storyHolder) {
        if (storyHolder != null) {
            bindAuthor(storyHolder.getUserObject());
            bindLikes(storyHolder.getLikes());
            bindContributions(storyHolder.getContributions());
        } else {
            bindAuthor(null);
            bindLikes(null);
            bindContributions(null);
        }
    }

    private void bindLikes(Integer count) {
        likeCount.setText((count == null) ? "0" : String.valueOf(count));
        final String storyKey = story.getKey();

        if (onStoryLikesBindingListener == null) {
            return;
        }
        if (onStoryLikesBindingListener.checkStoryKey(storyKey)) {
            toggleLikeIcon(onStoryLikesBindingListener.checkLike(storyKey));
            return;
        }
        storyServices.checkLikeForUser(story, new ValueEventListenerAdapter() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);
                onStoryLikesBindingListener.addLike(storyKey, dataSnapshot.exists());
                toggleLikeIcon(onStoryLikesBindingListener.checkLike(storyKey));
            }
        });
    }

    private void bindContributions(Integer count) {
        contributionsCount.setText((count == null) ? "0" : String.valueOf(count));
    }

    private void bindMarkers(Story story) {
        if (story.getMarkers() != null && story.getMarkers().length() > 0) {
            markers.setText(story.getMarkers());
            markers.setVisibility(View.VISIBLE);
        } else {
            markers.setVisibility(View.GONE);
        }
    }

    private void bindAuthor(User user) {
        if (user != null) {
            ImageLoader.loadPersonPictureToImageView(authorPicture, user.getPicture());
            authorName.setText(user.getNickname());
        } else {
            authorPicture.setImageResource(R.drawable.face);
            authorName.setText("");
        }
    }

    private void bindCover(@Nullable final Media cover) {
        if (cover != null) {
            coverImage.setVisibility(View.VISIBLE);
            coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

            switch (cover.getType()) {
                case Video:
                case VideoPhone:
                case Picture:
                    ImageLoader.loadGenericPictureToImageViewFit(coverImage, getCoverUrl(cover));
                    break;
                default:
                    coverImage.setVisibility(View.GONE);
            }
        } else {
            coverImage.setVisibility(View.GONE);
        }
    }

    private String getCoverUrl(final Media cover) {
        switch (cover.getType()) {
            case VideoPhone:
                return cover.getThumbnail();
            default:
                return cover.getUrl();
        }
    }

    private void toggleLikeIcon(final boolean like) {
        final Context context = itemView.getContext();
        final Drawable icon;

        if (like) {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_favorite);
            if (icon != null) {
                icon.setColorFilter(new PorterDuffColorFilter(primaryColorRes, PorterDuff.Mode.SRC_IN));
            }
        } else {
            icon = ContextCompat.getDrawable(context, R.drawable.ic_favorite_border);
        }

        likeCount.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            likeCount.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
        }
    }

    private View.OnClickListener onReadFullStoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (onStoryViewListener != null) {
                onStoryViewListener.onStoryViewClick(story);
            }
        }
    };

    private View.OnClickListener onUserClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            userViewManager.showUserInfo(story.getUserObject(), onUserStartChattingListener);
        }
    };

    public interface OnStoryLikesBindingListener {
        void addLike(String storyKey, final Boolean like);
        boolean checkLike(String storyKey);
        boolean checkStoryKey(String storyKey);
    }

}

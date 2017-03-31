package androks.simplywash.fragments;


import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import androks.simplywash.R;
import androks.simplywash.enums.SocialNetwork;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareFragment extends Fragment {

    public static final String SHARE_URL = "Some share url";
    public static final String FB_PACHAGE_NAME = "com.facebook.katana";
    public static final String VK_PACHAGE_NAME = "com.vkontakte.android";
    public static final String TW_PACHAGE_NAME = "com.twitter.android";

    private Unbinder unbinder;

    public ShareFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        return rootView;
    }

    @OnClick({R.id.vk, R.id.fb, R.id.twitter})
    public void shareVk(ImageView button){
        String packageName;
        switch (button.getId()){
            case R.id.vk:
                packageName = VK_PACHAGE_NAME;
                break;
            case R.id.fb:
                packageName = FB_PACHAGE_NAME;
                break;
            case R.id.twitter:
                packageName = TW_PACHAGE_NAME;
                break;
            default:
                packageName = FB_PACHAGE_NAME;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, SHARE_URL);
        boolean socialAppFound = false;
        List<ResolveInfo> matches = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith(packageName)) {
                intent.setPackage(info.activityInfo.packageName);
                socialAppFound = true;
                break;
            }
        }
        if (socialAppFound) {
            startActivity(intent);
        } else {
            SocialNetwork network = SocialNetwork.Vk;
            shareWithWebIntent(network);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void shareWithWebIntent(SocialNetwork socialNetwrkId) {
        String shareUrl = null;
        switch (socialNetwrkId) {
            case Facebook:
                shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + SHARE_URL;
                break;
            case Twitter:
                shareUrl = "https://twitter.com/intent/tweet?text=" + SHARE_URL;
                break;
            case Vk:
                shareUrl = "http://vkontakte.ru/share.php?url=" + SHARE_URL;
                break;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(shareUrl));
        startActivity(intent);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }
}

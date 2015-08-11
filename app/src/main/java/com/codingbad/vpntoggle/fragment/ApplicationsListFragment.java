package com.codingbad.vpntoggle.fragment;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codingbad.library.fragment.AbstractFragment;
import com.codingbad.vpntoggle.activity.R;
import com.codingbad.vpntoggle.adapter.ItemsAdapter;
import com.codingbad.vpntoggle.model.ApplicationItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import roboguice.inject.InjectView;

/**
 * Created by ayi on 6/26/15.
 */
public class ApplicationsListFragment extends AbstractFragment<ApplicationsListFragment.Callbacks> {

    private static final String LIST_STATE = "listState";
    @InjectView(R.id.fragment_list_recyclerview)
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Parcelable listState;

    public static Fragment newInstance() {
        return new ApplicationsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_applicationslist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (listState != null) {
            layoutManager.onRestoreInstanceState(listState);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setHasFixedSize(true);
        // set layout manager which positions items in the screen
        layoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // ItemAnimator animates views
        recyclerView.setItemAnimator(new FadeInAnimator());

        ItemsAdapter adapter = new ItemsAdapter();
        adapter.addItemList(getDeviceApplications());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(LIST_STATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        listState = layoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE, listState);
    }

    private ArrayList<ApplicationItem> getDeviceApplications() {
        final PackageManager packageManager = getActivity().getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        Map<Integer, ApplicationItem> applicationItemMap = new HashMap<Integer, ApplicationItem>();
        for (ApplicationInfo applicationInfo : installedApplications) {
            if (PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.INTERNET, applicationInfo.packageName)) {
                String appName = (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "(unknown)");
                Uri appIconUri = null;

                if (applicationInfo.icon != 0) {
                    appIconUri = Uri.parse("android.resource://" + applicationInfo.packageName + "/" + applicationInfo.icon);
                }

                int appUid = applicationInfo.uid;

                if (applicationItemMap.containsKey(appUid)) {
                    ApplicationItem applicationItem = applicationItemMap.get(appUid);
                    applicationItem.addApplication(appName);
                    applicationItemMap.put(appUid, applicationItem);
                } else {
                    applicationItemMap.put(appUid, new ApplicationItem(appIconUri, appName));
                }
            }
        }

        return new ArrayList<ApplicationItem>(applicationItemMap.values());
    }

    public interface Callbacks {
    }
}

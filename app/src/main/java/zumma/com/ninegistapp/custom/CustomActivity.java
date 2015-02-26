package zumma.com.ninegistapp.custom;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import zumma.com.ninegistapp.R;
import zumma.com.ninegistapp.utils.TouchEffect;


public class CustomActivity extends FragmentActivity
        implements OnClickListener {
    public static final TouchEffect TOUCH = new TouchEffect();
    private static final String TAG = CustomActivity.class.getSimpleName();

    public void onClick(View paramView) {
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setupActionBar();
    }

    public boolean onCreateOptionsMenu(Menu paramMenu) {
        getMenuInflater().inflate(R.menu.main, paramMenu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
//        if (paramMenuItem.getItemId() == 16908332)
//            finish();
        return super.onOptionsItemSelected(paramMenuItem);
    }

    public View setClick(int paramInt) {
        View localView = findViewById(paramInt);
        localView.setOnClickListener(this);
        return localView;
    }

    public View setTouchNClick(int paramInt) {
        View localView = setClick(paramInt);
        localView.setOnTouchListener(TOUCH);
        return localView;
    }

    protected void setupActionBar() {
        ActionBar localActionBar = getActionBar();
        if (localActionBar == null)
            return;
        localActionBar.setDisplayHomeAsUpEnabled(true);
        localActionBar.setHomeButtonEnabled(true);
//        localActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
//        localActionBar.setIcon(R.drawable.ic_launcher);
    }
}

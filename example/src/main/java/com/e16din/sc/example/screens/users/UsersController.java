package com.e16din.sc.example.screens.users;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.e16din.sc.ScreensController;
import com.e16din.sc.annotations.OnBind;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.example.R;

import java.util.Arrays;
import java.util.List;

@ViewController(screen = UsersScreen.class)
public class UsersController {

    @OnBind
    public void onBindView(ScreensController sc, View view, Object data) {
        Log.e("debug", "UsersController: !!!");
        RecyclerView vUsersList = view.findViewById(R.id.vUsersList);
        vUsersList.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(view.getContext());
        vUsersList.setLayoutManager(lm);

        User user1 = new User("Alex");
        User user2 = new User("Ura");
        User user3 = new User("Nikita");

        List<User> user = Arrays.asList(user1, user2, user3);
        UsersAdapter adapter = new UsersAdapter(user);
        vUsersList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private List<User> items;

    public UsersAdapter(List<User> items) {
        super();
        this.items = items;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup vParent, int viewType) {
        final View view = LayoutInflater.from(vParent.getContext())
                .inflate(R.layout.item_user, vParent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User item = getItem(position);
        holder.vNameLabel.setText(item.getName());
    }

    private User getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}

class UserViewHolder extends RecyclerView.ViewHolder {
    TextView vNameLabel;

    public UserViewHolder(View vItem) {
        super(vItem);
        vNameLabel = vItem.findViewById(R.id.vNameLabel);
    }
}



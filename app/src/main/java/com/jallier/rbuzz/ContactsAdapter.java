package com.jallier.rbuzz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {
    private List<Contact> contactList;

    public ContactsAdapter(List<Contact> ContactsList) {
        this.contactList = ContactsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_contacts, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Contact Contact = contactList.get(position);
        holder.fullName.setText(Contact.getFullName());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fullName;

        public MyViewHolder(View view) {
            super(view);
            fullName = (TextView) view.findViewById(R.id.rvName);
        }
    }
}

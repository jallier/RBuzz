package com.jallier.rbuzz;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

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

        ColorGenerator generator = ColorGenerator.MATERIAL;
        String letter = String.valueOf(Contact.getFullName().charAt(0)).toUpperCase();
        TextDrawable drawable = TextDrawable.builder().buildRound(letter, generator.getRandomColor());
        holder.letter.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView letter;
        public TextView fullName;

        public MyViewHolder(View view) {
            super(view);
            letter = (ImageView) view.findViewById(R.id.gmailItemLetter);
            fullName = (TextView) view.findViewById(R.id.rvName);
        }
    }
}

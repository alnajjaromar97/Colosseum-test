package com.stygian.gamingcenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    final int BG=Color.rgb(11,14,20), PANEL=Color.rgb(18,23,34), BORDER=Color.rgb(39,49,67),
        TEXT=Color.rgb(241,245,251), MUTED=Color.rgb(141,154,175), CYAN=Color.rgb(67,214,208),
        BLUE=Color.rgb(75,141,255), GREEN=Color.rgb(65,214,160), RED=Color.rgb(255,99,119),
        AMBER=Color.rgb(255,199,106);
    DB helper; SQLiteDatabase db;
    LinearLayout root, content, nav;
    String currentTab="الرئيسية";
    final String[] tabs={"الرئيسية","الأجهزة","الزبائن","الكافيتيريا","الطلبات","الموظفون"};
    final String[] roles={"admin","manager","cashier","cafe","technician"};
    final Map<String,String> roleNames=new LinkedHashMap<>();
    public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        helper=new DB(this); db=helper.getWritableDatabase();
        roleNames.put("admin","مدير النظام");roleNames.put("manager","مدير");roleNames.put("cashier","كاشير");roleNames.put("cafe","كافيتيريا");roleNames.put("technician","فني صيانة");
        showLogin();
    }
    GradientDrawable shape(int color,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);if(stroke!=0)d.setStroke(1,stroke);return d;}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.RIGHT);t.setTextDirection(View.TEXT_DIRECTION_RTL);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void pad(View v,int n){v.setPadding(n,n,n,n);}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    Button button(String label,int color){Button b=new Button(this);b.setText(label);b.setTextColor(TEXT);b.setAllCaps(false);b.setTextSize(13);b.setBackground(shape(color,0,18));return b;}
    EditText edit(String hint){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(MUTED);e.setTextColor(TEXT);e.setTextSize(14);e.setSingleLine(true);e.setPadding(14,7,14,7);e.setBackground(shape(Color.rgb(13,18,27),BORDER,14));return e;}
    void showLogin(){
        root=column();root.setBackgroundColor(BG);root.setPadding(24,50,24,24);setContentView(root);
        TextView logo=text("GC  GAME CENTER",24,CYAN,true);logo.setGravity(Gravity.CENTER);root.addView(logo,lp(-1,70));
        TextView subtitle=text("إدارة محل الألعاب والكافيتيريا",15,MUTED,false);subtitle.setGravity(Gravity.CENTER);root.addView(subtitle);
        Space space=new Space(this);root.addView(space,lp(1,36));
        EditText user=edit("اسم المستخدم");root.addView(user,lp(-1,54));
        EditText pass=edit("كلمة المرور");pass.setInputType(129);LinearLayout.LayoutParams p=lp(-1,54);p.topMargin=12;root.addView(pass,p);
        Button login=button("تسجيل الدخول",BLUE);p=lp(-1,54);p.topMargin=18;root.addView(login,p);
        TextView note=text("حساب تجريبي محلي: admin / ChangeMe123!",11,MUTED,false);note.setGravity(Gravity.CENTER);p=lp(-1,48);root.addView(note,p);
        login.setOnClickListener(v->{Cursor c=db.rawQuery("SELECT id,display_name,role,password FROM staff WHERE username=? AND active=1",new String[]{user.getText().toString().trim()});
            if(c.moveToFirst() && pass.getText().toString().equals(c.getString(3))){String name=c.getString(1),role=c.getString(2);c.close();showApp(name,role);}
            else{c.close();new AlertDialog.Builder(this).setMessage("اسم المستخدم أو كلمة المرور غير صحيحة").setPositiveButton("حسناً",null).show();}
        });
    }
    void showApp(String name,String role){
        root=column();root.setBackgroundColor(BG);setContentView(root);
        LinearLayout header=row();header.setPadding(18,15,18,12);header.setBackgroundColor(Color.rgb(15,19,28));
        LinearLayout titles=column();titles.addView(text("GAME CENTER",18,CYAN,true));titles.addView(text(name+" · "+roleNames.getOrDefault(role,role),11,MUTED,false));
        header.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
        Button logout=button("خروج",Color.rgb(37,46,61));header.addView(logout,lp(70,44));logout.setOnClickListener(v->showLogin());root.addView(header);
        ScrollView scroll=new ScrollView(this);content=column();content.setPadding(15,16,15,18);scroll.setFillViewport(true);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);nav=row();nav.setPadding(8,7,8,7);nav.setBackgroundColor(Color.rgb(15,19,28));
        for(String t:tabs){Button b=button(t,t.equals(currentTab)?BLUE:Color.rgb(37,46,61));LinearLayout.LayoutParams q=lp(-2,45);q.setMargins(4,0,4,0);nav.addView(b,q);b.setOnClickListener(v->{currentTab=t;showApp(name,role);});}
        hsv.addView(nav);root.addView(hsv);
        renderTab(role);
    }
    void title(String s,String sub){content.addView(text(s,24,TEXT,true));TextView t=text(sub,12,MUTED,false);LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=5;p.bottomMargin=18;content.addView(t,p);}
    void card(String label,String value,String hint){LinearLayout c=column();c.setPadding(16,14,16,14);c.setBackground(shape(PANEL,BORDER,18));TextView l=text(label,12,MUTED,false);c.addView(l);TextView v=text(value,27,TEXT,true);LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=4;c.addView(v,p);c.addView(text(hint,10,CYAN,false));LinearLayout.LayoutParams cp=lp(0,-2);cp.weight=1;cp.setMargins(4,4,4,4);statRow.addView(c,cp);}
    LinearLayout statRow;
    void renderTab(String role){
        content.removeAllViews();
        switch(currentTab){
            case "الرئيسية": dashboard();break;
            case "الأجهزة": devices(role);break;
            case "الزبائن": customers(role);break;
            case "الكافيتيريا": products(role);break;
            case "الطلبات": orders(role);break;
            case "الموظفون": staff(role);break;
        }
    }
    int count(String sql){Cursor c=db.rawQuery(sql,null);int n=c.moveToFirst()?c.getInt(0):0;c.close();return n;}
    void dashboard(){
        title("لوحة التحكم","ملخص سريع لحالة المحل");
        statRow=row();content.addView(statRow);card("أجهزة PC","54","Normal · VIP · MV");
        card("PlayStation","4","غرف خاصة");
        statRow=row();content.addView(statRow);card("الأجهزة المشغولة",""+count("SELECT COUNT(*) FROM devices WHERE status='busy'"),"جلسات فعالة");
        card("طلبات مفتوحة",""+count("SELECT COUNT(*) FROM orders WHERE status IN ('new','preparing','ready')"),"الكافيتيريا");
        section("اختصارات");
        for(String t:new String[]{"الأجهزة","الزبائن","الكافيتيريا","الطلبات","الموظفون"}){Button b=button("فتح "+t,Color.rgb(24,35,51));LinearLayout.LayoutParams p=lp(-1,48);p.setMargins(0,4,0,4);content.addView(b,p);b.setOnClickListener(v->{currentTab=t;renderTab("admin");});}
        section("تنبيه");content.addView(text("هذه نسخة أولية على جهاز أندرويد واحد. المزامنة بين عدة أجهزة وقفل أجهزة PC عن بُعد يحتاجان خادماً محلياً وعميل Windows منفصلاً.",12,AMBER,false));
    }
    void section(String s){TextView t=text(s,17,TEXT,true);LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=18;p.bottomMargin=9;content.addView(t,p);}
    void devices(String role){
        title("الأجهزة والجلسات","58 نقطة خدمة · اختر الجهاز لبدء أو إنهاء جلسة");
        String[] groups={"Normal","VIP","Master VIP","Private Room"};
        for(String group:groups){section(group.equals("Private Room")?"PLAYSTATION PRIVATE ROOMS":group.toUpperCase(Locale.ROOT));
            Cursor c=db.rawQuery("SELECT d.id,d.code,d.kind,d.tier,d.section,d.smoking,d.status,d.active_session_id,s.started_at,s.planned_minutes,s.rate_per_hour,COALESCE(c.name,'زبون غير مسجل') FROM devices d LEFT JOIN sessions s ON s.id=d.active_session_id AND s.status='active' LEFT JOIN customers c ON c.id=s.customer_id WHERE "+(group.equals("Private Room")?"d.kind='PlayStation'":"d.tier=?")+" ORDER BY d.code",group.equals("Private Room")?null:new String[]{group});
            while(c.moveToNext()){
                int id=c.getInt(0);String code=c.getString(1),kind=c.getString(2),tier=c.getString(3),section=c.getString(4),smoking=c.getString(5),status=c.getString(6);long sid=c.getLong(7);
                LinearLayout box=column();box.setPadding(14,12,14,12);box.setBackground(shape(status.equals("busy")?Color.rgb(47,39,25):PANEL,BORDER,16));
                LinearLayout top=row();top.addView(text(code,19,CYAN,true),new LinearLayout.LayoutParams(0,-2,1));top.addView(text(status.equals("busy")?"مشغول":"متاح",12,status.equals("busy")?AMBER:GREEN,true));box.addView(top);
                box.addView(text(kind+" · "+section+" · "+(smoking.equals("forbidden")?"🚭 ممنوع التدخين":smoking.equals("allowed")?"🚬 مسموح التدخين":"غرفة خاصة"),11,MUTED,false));
                if(status.equals("busy")){
                    box.addView(text(c.getString(11)+" · بدأت "+c.getString(8),12,TEXT,false));
                    Button end=button("إنهاء الجلسة",RED);box.addView(end,lp(-1,44));end.setOnClickListener(v->endSession(sid,code));
                }else{
                    Button start=button("بدء جلسة",BLUE);box.addView(start,lp(-1,44));start.setOnClickListener(v->startSession(id,code));
                }
                LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,4,0,7);content.addView(box,p);
            }c.close();
        }
    }
    void startSession(int deviceId,String code){
        Cursor c=db.rawQuery("SELECT id,name FROM customers ORDER BY name",null);ArrayList<String> names=new ArrayList<>();ArrayList<Integer> ids=new ArrayList<>();while(c.moveToNext()){ids.add(c.getInt(0));names.add(c.getString(1));}c.close();
        LinearLayout form=column();form.setPadding(8,8,8,8);
        Spinner customer=new Spinner(this);ArrayList<String> opts=new ArrayList<>();opts.add("بدون حساب زبون");opts.addAll(names);customer.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opts));form.addView(customer);
        EditText mins=edit("مدة الجلسة بالدقائق (اتركها فارغة للمفتوح)");form.addView(mins);
        EditText rate=edit("سعر الساعة بالدينار");rate.setInputType(8194);form.addView(rate);
        new AlertDialog.Builder(this).setTitle("بدء جلسة "+code).setView(form).setNegativeButton("إلغاء",null).setPositiveButton("بدء", (d,w)->{
            Integer cid=customer.getSelectedItemPosition()>0?ids.get(customer.getSelectedItemPosition()-1):null;
            ContentValues s=new ContentValues();s.put("device_id",deviceId);if(cid!=null)s.put("customer_id",cid);s.put("started_at",now());if(!mins.getText().toString().trim().isEmpty())s.put("planned_minutes",Integer.parseInt(mins.getText().toString().trim()));s.put("rate_per_hour",parseDouble(rate.getText().toString()));s.put("status","active");
            long sid=db.insert("sessions",null,s);ContentValues dv=new ContentValues();dv.put("status","busy");dv.put("active_session_id",sid);db.update("devices",dv,"id=?",new String[]{""+deviceId});renderTab("admin");
        }).show();
    }
    void endSession(long sid,String code){
        Cursor c=db.rawQuery("SELECT started_at,rate_per_hour,device_id FROM sessions WHERE id=?",new String[]{""+sid});if(!c.moveToFirst()){c.close();return;}
        String started=c.getString(0);double rate=c.getDouble(1);int did=c.getInt(2);c.close();
        long mins=Math.max(1,(System.currentTimeMillis()-parseTime(started))/60000);double total=mins/60.0*rate;
        Cursor oc=db.rawQuery("SELECT COALESCE(SUM(total),0) FROM orders WHERE session_id=? AND status!='cancelled'",new String[]{""+sid});if(oc.moveToFirst())total+=oc.getDouble(0);oc.close();
        ContentValues sv=new ContentValues();sv.put("ended_at",now());sv.put("status","ended");sv.put("total",total);db.update("sessions",sv,"id=?",new String[]{""+sid});
        ContentValues dv=new ContentValues();dv.put("status","available");dv.putNull("active_session_id");db.update("devices",dv,"id=?",new String[]{""+did});
        new AlertDialog.Builder(this).setTitle("انتهت جلسة "+code).setMessage("المدة: "+mins+" دقيقة\nالإجمالي التقريبي: "+String.format(Locale.US,"%.2f",total)+" د.أ").setPositiveButton("حسناً",(d,w)->renderTab("admin")).show();
    }
    long parseTime(String s){try{return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).parse(s).getTime();}catch(Exception e){return System.currentTimeMillis();}}
    String now(){return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date());}
    double parseDouble(String s){try{return Double.parseDouble(s.trim());}catch(Exception e){return 0;}}
    void customers(String role){
        title("الزبائن","ملفات الزبائن محفوظة على هذا الجهاز");
        Button add=button("+ إضافة زبون",BLUE);content.addView(add,lp(-1,48));add.setOnClickListener(v->addCustomer());
        section("قائمة الزبائن");Cursor c=db.rawQuery("SELECT id,name,phone,balance FROM customers ORDER BY id DESC",null);
        while(c.moveToNext()){LinearLayout b=column();b.setPadding(14,12,14,12);b.setBackground(shape(PANEL,BORDER,14));b.addView(text("#"+c.getInt(0)+" · "+c.getString(1),15,TEXT,true));b.addView(text((c.getString(2)==null?"":c.getString(2))+"   الرصيد: "+String.format(Locale.US,"%.2f",c.getDouble(3))+" د.أ",11,MUTED,false));LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,3,0,5);content.addView(b,p);}c.close();
    }
    void addCustomer(){LinearLayout f=column();EditText name=edit("اسم الزبون");EditText phone=edit("رقم الهاتف");f.addView(name);f.addView(phone);new AlertDialog.Builder(this).setTitle("إضافة زبون").setView(f).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{if(name.getText().toString().trim().isEmpty())return;ContentValues v=new ContentValues();v.put("name",name.getText().toString().trim());v.put("phone",phone.getText().toString().trim());v.put("created_at",now());db.insert("customers",null,v);renderTab("admin");}).show();}
    void products(String role){
        title("الكافيتيريا والمخزون","إدارة المنتجات والأسعار والكميات");
        Button add=button("+ إضافة منتج",BLUE);content.addView(add,lp(-1,48));add.setOnClickListener(v->addProduct());
        Cursor c=db.rawQuery("SELECT id,name,category,price,stock FROM products ORDER BY category,name",null);
        while(c.moveToNext()){LinearLayout b=column();b.setPadding(14,12,14,12);b.setBackground(shape(PANEL,BORDER,14));b.addView(text(c.getString(1)+" · "+String.format(Locale.US,"%.2f",c.getDouble(3))+" د.أ",15,TEXT,true));b.addView(text(c.getString(2)+" · المخزون: "+c.getInt(4),11,MUTED,false));LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,4,0,4);content.addView(b,p);}c.close();
    }
    void addProduct(){LinearLayout f=column();EditText n=edit("اسم المنتج"),cat=edit("الفئة"),price=edit("السعر بالدينار"),stock=edit("الكمية");price.setInputType(8194);stock.setInputType(2);f.addView(n);f.addView(cat);f.addView(price);f.addView(stock);new AlertDialog.Builder(this).setTitle("إضافة منتج").setView(f).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{if(n.getText().toString().trim().isEmpty())return;ContentValues v=new ContentValues();v.put("name",n.getText().toString().trim());v.put("category",cat.getText().toString().trim());v.put("price",parseDouble(price.getText().toString()));v.put("stock",(int)parseDouble(stock.getText().toString()));db.insert("products",null,v);renderTab("admin");}).show();}
    void orders(String role){
        title("طلبات الكافيتيريا","الطلبات المسجلة على هذا الجهاز");
        Cursor c=db.rawQuery("SELECT o.id,o.status,o.total,o.created_at,d.code,COALESCE(c.name,'زبون') FROM orders o LEFT JOIN devices d ON d.id=o.device_id LEFT JOIN customers c ON c.id=o.customer_id ORDER BY o.id DESC",null);
        while(c.moveToNext()){LinearLayout b=column();b.setPadding(14,12,14,12);b.setBackground(shape(PANEL,BORDER,14));b.addView(text("طلب #"+c.getInt(0)+" · "+c.getString(4),16,CYAN,true));b.addView(text(c.getString(5)+" · "+c.getString(1)+" · "+String.format(Locale.US,"%.2f",c.getDouble(2))+" د.أ",12,TEXT,false));b.addView(text(c.getString(3),10,MUTED,false));LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,4,0,4);content.addView(b,p);}c.close();
        if(count("SELECT COUNT(*) FROM orders")==0)content.addView(text("لا توجد طلبات بعد. واجهة الطلب من أجهزة الزبائن تحتاج ربطاً بالشبكة في المرحلة التالية.",12,MUTED,false));
    }
    void staff(String role){
        if(!role.equals("admin")&&!role.equals("manager")){title("الموظفون","هذه الصفحة للمدير فقط");return;}
        title("الموظفون والصلاحيات","حسابات منفصلة حسب الوظيفة");
        Button add=button("+ إضافة موظف",BLUE);content.addView(add,lp(-1,48));add.setOnClickListener(v->addStaff());
        Cursor c=db.rawQuery("SELECT id,username,display_name,role,active FROM staff ORDER BY id",null);
        while(c.moveToNext()){LinearLayout b=column();b.setPadding(14,12,14,12);b.setBackground(shape(PANEL,BORDER,14));b.addView(text(c.getString(2)+" · "+c.getString(1),15,TEXT,true));b.addView(text(roleNames.getOrDefault(c.getString(3),c.getString(3))+" · "+(c.getInt(4)==1?"نشط":"معطل"),11,MUTED,false));LinearLayout.LayoutParams p=lp(-1,-2);p.setMargins(0,4,0,4);content.addView(b,p);}c.close();
    }
    void addStaff(){LinearLayout f=column();EditText name=edit("الاسم الظاهر"),user=edit("اسم المستخدم"),pass=edit("كلمة المرور");pass.setInputType(129);Spinner role=new Spinner(this);String[] opts={"مدير","كاشير","كافيتيريا","فني صيانة"};role.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,opts));f.addView(name);f.addView(user);f.addView(pass);f.addView(role);new AlertDialog.Builder(this).setTitle("إضافة موظف").setView(f).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{if(user.getText().toString().trim().isEmpty()||pass.getText().length()<6)return;String[] r={"manager","cashier","cafe","technician"};ContentValues v=new ContentValues();v.put("username",user.getText().toString().trim());v.put("display_name",name.getText().toString().trim());v.put("password",pass.getText().toString());v.put("role",r[role.getSelectedItemPosition()]);v.put("active",1);db.insert("staff",null,v);renderTab("admin");}).show();}
    static class DB extends SQLiteOpenHelper {
        DB(Activity a){super(a,"gaming_center_android.db",null,1);}
        public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE staff(id INTEGER PRIMARY KEY AUTOINCREMENT,username TEXT UNIQUE,display_name TEXT,role TEXT,password TEXT,active INTEGER DEFAULT 1)");
            d.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,phone TEXT,balance REAL DEFAULT 0,created_at TEXT)");
            d.execSQL("CREATE TABLE devices(id INTEGER PRIMARY KEY AUTOINCREMENT,code TEXT UNIQUE,kind TEXT,tier TEXT,section TEXT,smoking TEXT,status TEXT DEFAULT 'available',active_session_id INTEGER)");
            d.execSQL("CREATE TABLE sessions(id INTEGER PRIMARY KEY AUTOINCREMENT,device_id INTEGER,customer_id INTEGER,started_at TEXT,ended_at TEXT,planned_minutes INTEGER,rate_per_hour REAL DEFAULT 0,status TEXT,total REAL DEFAULT 0)");
            d.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,category TEXT,price REAL,stock INTEGER,active INTEGER DEFAULT 1)");
            d.execSQL("CREATE TABLE orders(id INTEGER PRIMARY KEY AUTOINCREMENT,session_id INTEGER,device_id INTEGER,customer_id INTEGER,status TEXT,total REAL,created_at TEXT)");
            d.execSQL("CREATE TABLE order_items(id INTEGER PRIMARY KEY AUTOINCREMENT,order_id INTEGER,product_id INTEGER,product_name TEXT,quantity INTEGER,unit_price REAL)");
            ContentValues a=new ContentValues();a.put("username","admin");a.put("display_name","مدير النظام");a.put("role","admin");a.put("password","ChangeMe123!");a.put("active",1);d.insert("staff",null,a);
            for(int i=1;i<=8;i++)device(d,"A"+i,"PC","Normal","A","na");
            for(int i=1;i<=10;i++)device(d,"B"+i,"PC","Normal","B","na");
            for(int i=1;i<=17;i++)device(d,"VIP"+i,"PC","VIP","VIP - Smoking","allowed");
            for(int i=1;i<=8;i++)device(d,"VIPNS"+i,"PC","VIP","VIP - No Smoking","forbidden");
            for(int i=1;i<=6;i++)device(d,"MV"+i,"PC","Master VIP","MV - No Smoking","forbidden");
            for(int i=1;i<=5;i++)device(d,"MVS"+i,"PC","Master VIP","MVS - Smoking","allowed");
            for(int i=1;i<=4;i++)device(d,"PS"+i,"PlayStation","Private Room","PS Private","na");
            product(d,"شيبس","سناكات",0.5,30);product(d,"شوكولاتة","سناكات",0.75,25);product(d,"مياه","مشروبات",0.25,50);product(d,"مشروب غازي","مشروبات",0.5,40);product(d,"مشروب طاقة","مشروبات",1.25,20);
        }
        void device(SQLiteDatabase d,String code,String kind,String tier,String section,String smoke){ContentValues v=new ContentValues();v.put("code",code);v.put("kind",kind);v.put("tier",tier);v.put("section",section);v.put("smoking",smoke);d.insert("devices",null,v);}
        void product(SQLiteDatabase d,String n,String cat,double price,int stock){ContentValues v=new ContentValues();v.put("name",n);v.put("category",cat);v.put("price",price);v.put("stock",stock);v.put("active",1);d.insert("products",null,v);}
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
    }
}

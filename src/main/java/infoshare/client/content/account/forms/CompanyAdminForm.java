package infoshare.client.content.account.forms;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import infoshare.app.util.fields.UIComboBoxHelper;
import infoshare.app.util.fields.UIComponentHelper;
import infoshare.client.content.MainLayout;
import infoshare.client.content.account.model.AdminModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by hashcode on 2015/11/24.
 */
public class CompanyAdminForm extends FormLayout implements Button.ClickListener {
    private final AdminModel bean;
    public final BeanItem<AdminModel> item;
    public final FieldGroup binder;
    final private MainLayout main;
    private final Company company;
    // Define Buttons
    public Button save = new Button("Save");
    public Button edit = new Button("Edit");
    public Button cancel = new Button("Cancel");
    public Button update = new Button("Update");
    public Button delete = new Button("Delete");

//    private String firstName;
//    private String middleName;
//    @NotNull
//    private String emailAddress;
//    @NotNull
//    private String lastName;
//    @NotNull
//    private String name;

    public CompanyAdminForm(MainLayout main, Company company) {
        this.main = main;
        this.company = company;
        bean = new AdminModel();
        item = new BeanItem<>(bean);
        binder = new FieldGroup(item);
        final UIComponentHelper UIComponent = new UIComponentHelper();
        final UIComboBoxHelper UIComboBox = new UIComboBoxHelper();

        TextField firstName = UIComponent.getGridTextField("First Name :", "firstName", AdminModel.class, binder);
        TextField middlename = UIComponent.getGridTextField("Middle Name :", "middleName", AdminModel.class, binder);
        TextField lastname = UIComponent.getGridTextField("Last Name:", "lastName", AdminModel.class, binder);
        TextField emailAddress = UIComponent.getGridTextField("Email Address :", "emailAddress", AdminModel.class, binder);

        //ComboBox Fields
        final ComboBox title = UIComboBox.getComboBox("Title :", "title", AdminModel.class, binder, new Consumer<ComboBox>() {
            public void accept(ComboBox comboBox) {
                Set<Title> titles = DemographicsFacade.titleListService.findAll();
                for (Title title : titles) {
                    comboBox.addItem(title.getId());
                    comboBox.setItemCaption(title.getId(), title.getName());
                }
            }
        });

        // Create a field group and use it to bind the fields in the layout
        GridLayout grid = new GridLayout(4, 10);
        grid.setSizeFull();

        // First ROW
        grid.addComponent(title, 0, 0);
        grid.addComponent(firstName, 1, 0);
        grid.addComponent(lastname, 2, 0);

        //Second ROW
        grid.addComponent(middlename, 0, 1);
        grid.addComponent(emailAddress, 1, 1);

        //SPAN ROW 1 and 2
//        grid.addComponent(description, 2, 0, 2, 1);

        HorizontalLayout buttons = ButtonsHelper.getButtons(save, edit, cancel, update, delete);
        buttons.setSizeFull();
        grid.addComponent(new Label("<hr/>", ContentMode.HTML), 0, 4, 2, 4);
        grid.addComponent(buttons, 0, 5, 2, 5);
        addComponent(grid);
        addListeners();
    }


    @Override
    public void buttonClick(Button.ClickEvent event) {
        final Button source = event.getButton();
        if (source == save) {
            saveForm(binder);
        } else if (source == edit) {
            setEditFormProperties();
        } else if (source == cancel) {
            getHome();
        } else if (source == update) {
            saveEditedForm(binder);
        } else if (source == delete) {
            deleteForm(binder);
        }
    }

    private void deleteForm(FieldGroup binder) {

    }


    private void saveEditedForm(FieldGroup binder) {

    }

    private void getHome() {
        main.content.setSecondComponent(new AccountMenu(main, "LANDING"));
    }

    private void saveForm(FieldGroup binder) {
        try {
            binder.commit();
        } catch (FieldGroup.CommitException e) {
            e.printStackTrace();
        }

        final AdminModel bean = ((BeanItem<AdminModel>) binder.getItemDataSource()).getBean();
        final Company updateCompany = new Company
                .Builder()
                .copy(company)
                .adminattached(DomainState.WITH_ADMIN.name())
                .build();
        OfficeFacade.companyService.save(updateCompany);


        final String password = SecurityService.generateRandomPassword();
        Map<String, String> stringVals = new HashMap<>();
        stringVals.put("firstName", bean.getFirstName());
        stringVals.put("middleName", bean.getMiddleName());
        stringVals.put("lastName", bean.getLastName());
        stringVals.put("name", bean.getTitle());
        stringVals.put("authvalue", PasswordHash.createEncryptedPassword(password));
        stringVals.put("emailAddress", bean.getEmailAddress());
        stringVals.put("company", company.getId());

        Map<String, Boolean> boolVals = new HashMap<>();
        boolVals.put("enabled", Boolean.TRUE);
        boolVals.put("accountNonExpired", Boolean.TRUE);
        boolVals.put("accountNonLocked", Boolean.TRUE);
        boolVals.put("credentialsNonExpired", Boolean.TRUE);
        Person companyAdmin = createAccount(stringVals, boolVals);
        SecurityService.sendEmail(password, companyAdmin);
        getHome();

    }


    private Person createAccount(Map<String, String> stringVals, Map<String, Boolean> boolVals) {
        Person companyAdmin = PersonFactory.getPerson(stringVals, boolVals);
        PeopleFacade.personService.save(companyAdmin);
        PersonRole role = PersonRoleFactory.getPersonRole(companyAdmin.getId(), RolesValues.ROLE_COMPANY_ADMIN.name());
        PeopleFacade.personRoleService.save(role);
        return companyAdmin;
    }

    private void setEditFormProperties() {
        binder.setReadOnly(false);
        save.setVisible(false);
        edit.setVisible(false);
        cancel.setVisible(true);
        delete.setVisible(false);
        update.setVisible(true);
    }

    private void addListeners() {
        //Register Button Listeners
        save.addClickListener(this);
        edit.addClickListener(this);
        cancel.addClickListener(this);
        update.addClickListener(this);
        delete.addClickListener(this);
    }

}
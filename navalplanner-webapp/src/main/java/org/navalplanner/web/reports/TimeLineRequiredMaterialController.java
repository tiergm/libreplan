/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.reports;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.materials.entities.Material;
import org.navalplanner.business.materials.entities.MaterialCategory;
import org.navalplanner.business.materials.entities.MaterialStatusEnum;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.web.common.components.ExtendedJasperreport;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;
import org.zkoss.zul.Treerow;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class TimeLineRequiredMaterialController extends
        NavalplannerReportController {

    private static final String REPORT_NAME = "timeLineRequiredMaterial";

    private ITimeLineRequiredMaterialModel timeLineRequiredMaterialModel;

    private Tree allCategoriesTree;

    private Datebox startingDate;

    private Datebox endingDate;

    private Combobox cbStatus;

    private Listbox lbOrders;

    List<MaterialCategory> filterCategories = new ArrayList<MaterialCategory>();

    List<Material> filterMaterials = new ArrayList<Material>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        prepareAllCategoriesTree();
    }

    public List<Order> getOrders() {
        return timeLineRequiredMaterialModel.getOrders();
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        return timeLineRequiredMaterialModel.getTimeLineRequiredMaterial(
                getStartingDate(), getEndingDate(), getSelectedStatus(),
                getSelectedOrders(), getSelectedCategories(),
                getSelectedMaterials());
    }

    private Date getStartingDate() {
        Date result = startingDate.getValue();
        if (result == null) {
            startingDate.setValue(new Date());
        }
        return startingDate.getValue();
    }

    private Date getEndingDate() {
        Date result = endingDate.getValue();
        if (result == null) {
            endingDate.setValue(getDefaultEndingDate());
        }
        return endingDate.getValue();
    }

    private Date getDefaultEndingDate(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartingDate());
        calendar.add(calendar.MONTH, 1);

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, month, date);
        return calendar.getTime();
    }

    private MaterialStatusEnum getSelectedStatus() {
        final Comboitem item = cbStatus.getSelectedItem();
        return (item != null) ? (MaterialStatusEnum) item.getValue() : null;
    }

    private String getSelectedStatusName() {
        if (getSelectedStatus() != null) {
            return getSelectedStatus().name();
        }
        return null;
    }

    private List<Order> getSelectedOrders() {
        List<Order> result = new ArrayList<Order>();
        final Set<Listitem> listItems = lbOrders.getSelectedItems();
        for (Listitem each : listItems) {
            result.add((Order) each.getValue());
        }
        return result;
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());
        result.put("status", getSelectedStatusName());
        return result;
    }

    public void showReport(ExtendedJasperreport jasperreport) {
        if (lbOrders.getSelectedCount() <= 0) {
            throw new WrongValueException(lbOrders, _("Please, select an order"));
        }
        super.showReport(jasperreport);
    }

    public MaterialStatusEnum[] getMaterialStatus() {
        return MaterialStatusEnum.values();
    }

    /**
     * Operations to filter by category and/or material
     */

    private void prepareAllCategoriesTree() {
        if (allCategoriesTree.getTreeitemRenderer() == null) {
            allCategoriesTree
                    .setTreeitemRenderer(getMaterialCategoryRenderer());
        }
        allCategoriesTree.setModel(getAllMaterialCategories());
    }

    public TreeModel getAllMaterialCategories() {
        return getModel().getAllMaterialCategories();
    }

    public void clearSelectionAllCategoriesTree() {
        allCategoriesTree.clearSelection();
    }

    public MaterialCategoryRenderer getMaterialCategoryRenderer() {
        return new MaterialCategoryRenderer();
    }

    private class MaterialCategoryRenderer implements TreeitemRenderer {

        /**
         * Copied verbatim from org.zkoss.zul.Tree;
         */
        @Override
        public void render(Treeitem ti, Object node) throws Exception {
            Label lblName = null;
            if (node instanceof MaterialCategory) {
                final MaterialCategory materialCategory = (MaterialCategory) node;
                lblName = new Label(materialCategory.getName());
            } else if (node instanceof Material) {
                final Material material = (Material) node;
                lblName = new Label(material.getDescription());
            }

            Treerow tr = null;
            ti.setValue(node);
            if (ti.getTreerow() == null) {
                tr = new Treerow();
                tr.setParent(ti);
                ti.setOpen(true); // Expand node
            } else {
                tr = ti.getTreerow();
                tr.getChildren().clear();
            }
            // Add category name
            Treecell cellName = new Treecell();
            lblName.setParent(cellName);
            cellName.setParent(tr);
        }
    }

    private ITimeLineRequiredMaterialModel getModel() {
        return this.timeLineRequiredMaterialModel;
    }

    private List<MaterialCategory> getSelectedCategories() {
        filterCategories.clear();
        Set<Treeitem> setItems = (Set<Treeitem>) allCategoriesTree
                .getSelectedItems();

        for (Treeitem ti : setItems) {
            if ((ti.getValue() != null)
                    && (ti.getValue() instanceof MaterialCategory)) {
                filterCategories.add((MaterialCategory) ti.getValue());
                addSubCategories((MaterialCategory) ti.getValue());
            }
        }
        return filterCategories;
    }

    private void addSubCategories(MaterialCategory category) {
        for (MaterialCategory subCategory : category.getSubcategories()) {
            filterCategories.add(subCategory);
            addSubCategories(subCategory);
        }
    }

    private List<Material> getSelectedMaterials() {
        List<Material> materials = new ArrayList<Material>();
        Set<Treeitem> setItems = (Set<Treeitem>) allCategoriesTree
                .getSelectedItems();
        for (Treeitem ti : setItems) {
            if ((ti.getValue() != null) && (ti.getValue() instanceof Material)
                    && (!isContainedInCategories((Material) ti.getValue()))) {
                materials.add((Material) ti.getValue());
            }
        }
        return materials;
    }

    private boolean isContainedInCategories(Material material) {
        if (material != null) {
            return getSelectedCategories().contains(material.getCategory());
        }
        return false;
    }
}

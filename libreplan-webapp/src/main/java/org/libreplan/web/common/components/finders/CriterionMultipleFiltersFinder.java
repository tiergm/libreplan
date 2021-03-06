/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.web.common.components.finders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.libreplan.business.hibernate.notification.PredefinedDatabaseSnapshots;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.springframework.beans.factory.annotation.Autowired;

public class CriterionMultipleFiltersFinder extends MultipleFiltersFinder {

    @Autowired
    private PredefinedDatabaseSnapshots databaseSnapshots;

    /**
     * Forces to mark the string as needing translation
     */
    private static String _(String string) {
        return string;
    }

    @Override
    public List<FilterPair> getFirstTenFilters() {
        getListMatching().clear();
        SortedMap<CriterionType, List<Criterion>> criterionsMap = getCriterionsMap();
        Iterator<CriterionType> iteratorCriterionType = criterionsMap.keySet()
                .iterator();
        while (iteratorCriterionType.hasNext() && getListMatching().size() < 10) {
            CriterionType type = iteratorCriterionType.next();
            for (int i = 0; getListMatching().size() < 10
                    && i < criterionsMap.get(type).size(); i++) {
                Criterion criterion = criterionsMap.get(type).get(i);
                addCriterion(type, criterion);
            }
        }
        return getListMatching();
    }

    private SortedMap<CriterionType, List<Criterion>> getCriterionsMap() {
        return databaseSnapshots.snapshotCriterionsMap();
    }

    @Override
    public List<FilterPair> getMatching(String filter) {
        getListMatching().clear();
        if ((filter != null) && (!filter.isEmpty())) {
            filter = StringUtils.deleteWhitespace(filter.toLowerCase());
            searchInCriterionTypes(filter);
        }
        addNoneFilter();
        return getListMatching();
    }

    private void searchInCriterionTypes(String filter) {
        boolean limited = (filter.length() < 3);
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        for (CriterionType type : mapCriterions.keySet()) {
            String name = StringUtils.deleteWhitespace(type.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                setFilterPairCriterionType(type, limited);
            } else {
                searchInCriterions(type, filter);
            }
        }
    }

    private void searchInCriterions(CriterionType type, String filter) {
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        for (Criterion criterion : mapCriterions.get(type)) {
            String name = StringUtils.deleteWhitespace(criterion.getName()
                    .toLowerCase());
            if (name.contains(filter)) {
                addCriterion(type, criterion);
                if ((filter.length() < 3) && (getListMatching().size() > 9)) {
                    return;
                }
            }
        }
    }

    private void setFilterPairCriterionType(CriterionType type, boolean limited) {
        Map<CriterionType, List<Criterion>> mapCriterions = getCriterionsMap();
        for (Criterion criterion : mapCriterions.get(type)) {
            addCriterion(type, criterion);
            if ((limited) && (getListMatching().size() > 9)) {
                return;
            }
        }
    }

    private void addCriterion(CriterionType type, Criterion criterion) {
        String pattern = criterion.getName() + " ( " + type.getName() + " ) ";
        getListMatching().add(
                new FilterPair(ResourceFilterEnum.Criterion, type.getResource()
                        .toLowerCase(), pattern, criterion));
    }
}
import React, { useState } from 'react';
import * as R from 'ramda';
import {
  ArrowDropDownOutlined,
  ArrowDropUpOutlined,
} from '@mui/icons-material';
import { useFormatter } from '../components/i18n';

const useSearchAnFilter = (schema, defaultSortKey, searchColumns) => {
  const { t } = useFormatter();
  const [order, setOrder] = useState({
    sortBy: defaultSortKey,
    orderAsc: true,
  });
  const [keyword, setKeyword] = useState('');
  const [tags, setTags] = useState([]);
  const handleAddTag = (value) => {
    setTags(R.uniq(R.append(value, tags)));
  };
  const handleRemoveTag = (value) => {
    const remainingTags = R.filter((n) => n.id !== value, tags);
    setTags(remainingTags);
  };
  const handleSearch = (value) => setKeyword(value);
  const reverseBy = (field) => {
    setOrder({ sortBy: field, orderAsc: !order.orderAsc });
  };
  const buildHeader = (field, label, isSortable, styles) => {
    const sortComponent = order.orderAsc ? (
      <ArrowDropDownOutlined style={styles.iconSort} />
    ) : (
      <ArrowDropUpOutlined style={styles.iconSort} />
    );
    if (isSortable) {
      return (
        <div style={styles[field]} onClick={() => reverseBy(field)}>
          <span>{t(label)}</span>
          {order.sortBy === field ? sortComponent : ''}
        </div>
      );
    }
    return (
      <div style={styles[field]}>
        <span>{t(label)}</span>
      </div>
    );
  };
  const filterAndSort = (elements) => {
    const filterByKeyword = (e) => {
      const isEmptyKeyword = keyword === '';
      const isKnownColumn = searchColumns
        .map((d) => e[`${schema}_${d}`] || '')
        .map((data) => (typeof data === 'object' ? JSON.stringify(data) : data))
        .map((info) => info.toLowerCase().indexOf(keyword.toLowerCase()) !== -1)
        .reduce((a, b) => a || b);
      return isEmptyKeyword || isKnownColumn;
    };
    const sort = R.sortWith(
      order.orderAsc
        ? [R.ascend(R.prop(order.sortBy))]
        : [R.descend(R.prop(order.sortBy))],
    );
    return R.pipe(
      R.filter(
        (n) => tags.length === 0
          || R.any(
            (filter) => R.includes(filter, n[`${schema}_tags`] || []),
            R.pluck('id', tags),
          ),
      ),
      R.filter(filterByKeyword),
      sort,
    )(elements);
  };
  return {
    keyword,
    order,
    tags,
    handleAddTag,
    handleRemoveTag,
    handleSearch,
    buildHeader,
    filterAndSort,
  };
};

export default useSearchAnFilter;
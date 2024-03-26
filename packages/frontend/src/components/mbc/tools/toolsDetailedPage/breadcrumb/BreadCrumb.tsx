import React from 'react';
import Styles from './BreadCrumb.scss';

const Breadcrumb = (props : any) => {
  return (
    <div className={Styles.breadcrumb}>
      <ol>
          <li><a href='#/'>Start</a></li>
          <li><a href='#/tools'>Tools</a></li>
          { props.children }
      </ol>
    </div>
  )
}

export default Breadcrumb;
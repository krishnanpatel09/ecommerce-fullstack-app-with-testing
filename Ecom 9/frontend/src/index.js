import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

reportWebVitals();

/* This code is the entry point for a React application
It imports the necessary dependencies:

    - React: The core React library
    - ReactDOM: For rendering React components to the DOM
    - ./index.css: A CSS file for styling the application
    - App: The main application component
    - reportWebVitals: The performance monitoring function we saw earlier

It creates a root React DOM node using ReactDOM.createRoot() and targets the HTML element with the ID 'root'.
It renders the App component inside React.StrictMode to the root DOM node
 */
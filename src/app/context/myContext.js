// app/contexts/MyContext.js
'use client'
import { createContext, useContext, useState } from 'react';

const MyContext = createContext();

export const useMyContext = () => useContext(MyContext);

export function MyContextProvider({ children }) {
  const [point, setPoint] = useState(10);

  return (
    <MyContext.Provider value={{ point, setPoint }}>
      {children}
    </MyContext.Provider>
  );
}
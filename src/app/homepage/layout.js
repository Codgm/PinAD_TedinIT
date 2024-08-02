import Navbar from "../components/nav";
import { MyContextProvider } from "../context/myContext";


export default function Layout({ children }) {
  return (
    <html>
      <body leftmargin="0"  topmargin="0" className="mx-auto min-h-screen items-center justify-center">
        <MyContextProvider>
          {children}
        </MyContextProvider>
      </body>
    </html>
  );
}

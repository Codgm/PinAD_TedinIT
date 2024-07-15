import Navbar from "../components/nav";


export default function Layout({ children }) {
  return (
    <html>
      <body leftmargin="0"  topmargin="0" className="mx-auto min-h-screen items-center justify-center">
          <Navbar/>
          {children}
      </body>
    </html>
  );
}

import Home from "./pages/Home.jsx";
import Results from "./pages/Result.jsx";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        <Route path="/" element={<Navigate to="/home"/>}/>
        <Route path="/home" element={<Home/>}/>
        <Route path="/results" element={<Results/>}/>

      </Routes>
    </BrowserRouter>
  );
}

export default App;
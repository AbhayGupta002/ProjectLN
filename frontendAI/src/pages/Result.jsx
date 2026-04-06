import { useLocation } from "react-router-dom";

function Results(){

  const location = useLocation();
  const data = location.state?.data;

  return (

    <div style={{padding:"40px"}}>

      <h2>AI Response</h2>

{/*       {JSON.stringify(data)} */}
      <pre>
              {JSON.stringify(data,null,2)}
      </pre>

    </div>

  );
}

export default Results;
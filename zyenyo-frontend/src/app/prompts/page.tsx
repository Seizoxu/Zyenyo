import Header from "@/components/Navigation/Header";
import PromptControls from "@/components/PromptsPage/PromptControls";

export default function PromptsPage() {
  return (
    <main className="flex min-h-screen flex-col items-center bg-zinc-800 text-white">
    <Header />
      <PromptControls/> 
    </main>
  )
}

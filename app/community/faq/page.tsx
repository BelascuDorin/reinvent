"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { trpc } from "@/lib/trpc"
import { ArrowLeft, MessageSquare, Plus, HelpCircle } from "lucide-react"
import Link from "next/link"

export default function FAQPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  const [newFAQ, setNewFAQ] = useState({ question: "", category: "" })
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: faqs, isLoading, refetch } = trpc.community.getFAQs.useQuery({})

  const addFAQMutation = trpc.community.addCustomFAQ.useMutation({
    onSuccess: () => {
      setIsDialogOpen(false)
      setNewFAQ({ question: "", category: "" })
      refetch()
    },
  })

  const handleAddFAQ = () => {
    if (!newFAQ.question.trim() || !newFAQ.category.trim()) return

    addFAQMutation.mutate({
      question: newFAQ.question,
      category: newFAQ.category,
    })
  }

  if (status === "loading" || isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div>Loading...</div>
      </div>
    )
  }

  if (!session) {
    return null
  }

  const isMentor = session.user.role === "mentor"

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Link href="/community">
              <Button variant="ghost" size="sm">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Community
              </Button>
            </Link>
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <MessageSquare className="w-5 h-5 text-primary-foreground" />
              </div>
              <h1 className="text-xl font-semibold text-foreground">FAQ Guide</h1>
            </div>
          </div>
          {isMentor && (
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="w-4 h-4 mr-2" />
                  Add FAQ
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add Custom FAQ</DialogTitle>
                  <DialogDescription>Suggest a question that mentees often ask in your field</DialogDescription>
                </DialogHeader>
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="category">Category</Label>
                    <Input
                      id="category"
                      value={newFAQ.category}
                      onChange={(e) => setNewFAQ((prev) => ({ ...prev, category: e.target.value }))}
                      placeholder="e.g., Technical Skills, Career Path"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="question">Question</Label>
                    <Textarea
                      id="question"
                      value={newFAQ.question}
                      onChange={(e) => setNewFAQ((prev) => ({ ...prev, question: e.target.value }))}
                      placeholder="What question do mentees often ask you?"
                      rows={3}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={handleAddFAQ} disabled={addFAQMutation.isLoading} className="flex-1">
                      {addFAQMutation.isLoading ? "Adding..." : "Add FAQ"}
                    </Button>
                    <Button variant="outline" onClick={() => setIsDialogOpen(false)}>
                      Cancel
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          )}
        </div>
      </header>

      <div className="container mx-auto px-4 py-8 max-w-4xl">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Questions to Ask Mentors</h2>
          <p className="text-muted-foreground">Great questions to help you make the most of your mentoring sessions</p>
        </div>

        {/* Introduction */}
        <Card className="mb-8">
          <CardHeader>
            <CardTitle className="flex items-center">
              <HelpCircle className="w-5 h-5 mr-2 text-primary" />
              How to Use This Guide
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3 text-muted-foreground">
              <p>
                Before your mentoring session, review these questions and choose the ones most relevant to your
                interests and goals.
              </p>
              <p>
                <strong>Pro tip:</strong> Prepare 3-5 specific questions rather than asking general ones like "What
                should I do?"
              </p>
              <p>Remember to listen actively and take notes during your session!</p>
            </div>
          </CardContent>
        </Card>

        {/* FAQ Categories */}
        {!faqs || Object.keys(faqs).length === 0 ? (
          <Card>
            <CardContent className="py-12 text-center">
              <MessageSquare className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-foreground mb-2">No FAQs Available</h3>
              <p className="text-muted-foreground">Check back later for helpful questions to ask mentors</p>
            </CardContent>
          </Card>
        ) : (
          <div className="space-y-6">
            {Object.entries(faqs).map(([category, questions]) => (
              <Card key={category}>
                <CardHeader>
                  <CardTitle>{category}</CardTitle>
                  <CardDescription>
                    {questions.length} question{questions.length !== 1 ? "s" : ""} in this category
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <Accordion type="single" collapsible className="w-full">
                    {questions.map((faq, index) => (
                      <AccordionItem key={faq.id} value={`item-${index}`}>
                        <AccordionTrigger className="text-left">{faq.question}</AccordionTrigger>
                        <AccordionContent>
                          <div className="space-y-2 text-muted-foreground">
                            <p>
                              <strong>Why ask this:</strong> This question helps you understand the practical aspects of
                              the role and what skills you should focus on developing.
                            </p>
                            <p>
                              <strong>Follow-up questions:</strong> "How can I start building these skills?" or "What
                              resources do you recommend?"
                            </p>
                          </div>
                        </AccordionContent>
                      </AccordionItem>
                    ))}
                  </Accordion>
                </CardContent>
              </Card>
            ))}
          </div>
        )}

        {/* Tips Section */}
        <Card className="mt-8">
          <CardHeader>
            <CardTitle>Tips for Great Mentoring Sessions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <h4 className="font-semibold text-foreground mb-2">Before the Session</h4>
                <ul className="space-y-1 text-sm text-muted-foreground">
                  <li>• Research the mentor's background</li>
                  <li>• Prepare specific questions</li>
                  <li>• Set clear goals for the conversation</li>
                  <li>• Have a notebook ready</li>
                </ul>
              </div>
              <div>
                <h4 className="font-semibold text-foreground mb-2">During the Session</h4>
                <ul className="space-y-1 text-sm text-muted-foreground">
                  <li>• Listen actively and take notes</li>
                  <li>• Ask follow-up questions</li>
                  <li>• Be respectful of time</li>
                  <li>• Share your own goals and interests</li>
                </ul>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
